package com.coste.syncorg.orgdata;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import com.coste.syncorg.gui.FileDecryptionActivity;
import com.coste.syncorg.util.FileUtils;
import com.coste.syncorg.util.Preferences;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OrgFileParser {
    public static final String BLOCK_SEPARATOR_PREFIX = "#HEAD#";
    private static final Pattern starPattern = Pattern.compile("^(\\**)\\s");
    private static final String fileMatchPattern = "\\[file:(.*?)\\]\\[(.*?)\\]\\]";
    private static final Pattern getTodos = Pattern
            .compile("#\\+TODO:([^\\|]+)(\\| (.*))*");
    private static final Pattern getPriorities = Pattern
            .compile("#\\+ALLPRIORITIES:([^\\n]+)");
    private static final Pattern getTags = Pattern.compile("#\\+TAGS:([^\\n]+)");
    static private OrgFileParser mInstance;
    Context context;
    private ContentResolver resolver;
    private OrgDatabase db;
    private ParseStack parseStack;
    private StringBuilder payload;
    private OrgFile orgFile;
    private OrgNodeParser orgNodeParser;
    private HashSet<String> excludedTags;
    private HashMap<Integer, Integer> position;
    private HashMap<OrgNodeTimeDate.TYPE, OrgNodeTimeDate> timestamps;

    private OrgFileParser(Context context) {
        this.db = OrgDatabase.getInstance();
        this.context = context;
        this.resolver = context.getContentResolver();
        timestamps = new HashMap<>();
    }

    static public OrgFileParser getInstance() {
        return mInstance;
    }

    static public void startParser(Context context) {
        mInstance = new OrgFileParser(context);
    }

    private static int numberOfStars(String thisLine) {
        Matcher matcher = starPattern.matcher(thisLine);
        if (matcher.find()) {
            return matcher.end(1) - matcher.start(1);
        } else
            return 0;
    }


    public static HashMap<String, Boolean> parseTodos(String line) {
        HashMap<String, Boolean> result = null;

        Matcher m = getTodos.matcher(line);
        if (m.find()) {
            result = new HashMap<>();
            String lastTodo = "";
            Boolean isDone = false;
            for (int idx = 1; idx <= m.groupCount(); idx++) {
                if (m.group(idx) != null && m.group(idx).trim().length() > 0) {
                    if (m.group(idx).contains("|")) {
                        isDone = true;
                        continue;
                    }
                    String[] grouping = m.group(idx).trim().split("\\s+");
                    for (String group : grouping) {
                        lastTodo = group.trim();
                        result.put(group.trim(), isDone);
                    }
                }
            }
            if (!isDone) {
                result.put(lastTodo, true);
            }
        }
        return result;
    }

    public static void decryptAndParseFile(OrgFile orgFile, BufferedReader reader, Context context) {
        try {
            Intent intent = new Intent(context, FileDecryptionActivity.class);
            intent.putExtra("data", FileUtils.read(reader).getBytes());
            intent.putExtra("filename", orgFile.filename);
            intent.putExtra("filenameAlias", orgFile.name);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        } catch (IOException e) {
        }
    }

    /**
     * Remove old file from DB, decrypt file if encrypted and then parse
     *
     * @param orgFile
     * @param breader
     * @param context
     */
    public static void parseFile(OrgFile orgFile, BufferedReader breader, Context context) {
        if (orgFile.isEncrypted())
            decryptAndParseFile(orgFile, breader, context);
        else {
            OrgFileParser.getInstance().parse(orgFile, breader, context);
        }
    }

    private void init(OrgFile orgFile) {
        orgFile.removeFile(context, false);
        orgFile.addFile(context);
        this.orgFile = orgFile;

        this.parseStack = new ParseStack();
        this.parseStack.add(0, orgFile.nodeId, "");

        this.payload = new StringBuilder();

        this.orgNodeParser = new OrgNodeParser(
                OrgProviderUtils.getTodos(resolver));

        this.position = new HashMap<>();
    }

    public void parse(OrgFile orgFile, BufferedReader breader, Context context) {
        this.excludedTags = Preferences.getExcludedTags();

        parse(orgFile, breader);
    }

    public void parse(OrgFile orgFile, BufferedReader breader) {
        init(orgFile);
        db.beginTransaction();

        try {
            String currentLine;
            while ((currentLine = breader.readLine()) != null) parseLine(currentLine);

            // Add payload to the final node
            db.fastInsertNodePayload(parseStack.getCurrentNodeId(), this.payload.toString());

        } catch (IOException e) {
        }

        db.endTransaction();

    }

    private void parseLine(String line) {
        if (TextUtils.isEmpty(line))
            return;

        int numstars = numberOfStars(line);

        if (numstars > 0) { // new node
            db.fastInsertNodePayload(parseStack.getCurrentNodeId(), this.payload.toString());
            this.payload = new StringBuilder();
            parseHeading(line, numstars);
        } else { // continuing previous node
            OrgProviderUtils.addTodos(parseTodos(line), resolver);
            parseTimestamps(line);
            payload.append(line).append("\n");
        }
    }

    private void parseHeading(String thisLine, int numstars) {
        int currentLevel = parseStack.getCurrentLevel();

        if (position.get(numstars - 1) == null) position.put(numstars - 1, 0);
        if (numstars <= currentLevel) {
            int value = position.get(numstars - 1);
            position.put(numstars - 1, value + 1);
        } else {
            position.put(numstars - 1, 0);
        }

        while (numstars <= parseStack.getCurrentLevel()) {
            parseStack.pop();
        }

        OrgNode node = this.orgNodeParser.parseLine(thisLine, numstars);
        node.tags_inherited = parseStack.getCurrentTags();
        node.fileId = orgFile.id;
        node.parentId = parseStack.getCurrentNodeId();
        node.position = position.get(numstars - 1);

        long newId = db.fastInsertNode(node);
        parseStack.add(numstars, newId, node.tags);
    }

    /**
     * Parse the line for any timestamps
     *
     * @param line
     * @return A HashMap<String,int> where first key is timestamp type and second is value.
     * Return null if no timestamp found.
     */
    private void parseTimestamps(String line) {
        timestamps.clear();
        for (OrgNodeTimeDate.TYPE type : OrgNodeTimeDate.TYPE.values()) {
            timestamps.put(type, new OrgNodeTimeDate(type, line));
        }
        db.fastInsertTimestamp(parseStack.getCurrentNodeId(), orgFile.id, timestamps);
    }

    private class ParseStack {
        private Stack<Item> stack;

        public ParseStack() {
            this.stack = new Stack<>();
        }

        public void add(int level, long nodeId, String tags) {
            stack.push(new Item(level, nodeId, stripTags(tags)));
        }

        private String stripTags(String tags) {
            if (excludedTags == null || TextUtils.isEmpty(tags))
                return tags;

            StringBuilder result = new StringBuilder();
            for (String tag : tags.split(":")) {
                if (!excludedTags.contains(tag)) {
                    result.append(tag);
                    result.append(":");
                }
            }

            if (!TextUtils.isEmpty(result))
                result.deleteCharAt(result.lastIndexOf(":"));

            return result.toString();
        }

        public void pop() {
            this.stack.pop();
        }

        public int getCurrentLevel() {
            return stack.peek().level;
        }

        public long getCurrentNodeId() {
            return stack.peek().nodeId;
        }

        public String getCurrentTags() {
            return stack.peek().tag;
        }

        private class Item {
            int level;
            long nodeId;
            String tag;

            public Item(Integer level, Long nodeId, String tags) {
                this.level = level;
                this.nodeId = nodeId;
                this.tag = tags;
            }
        }

    }

}
