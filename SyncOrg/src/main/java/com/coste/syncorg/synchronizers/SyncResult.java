package com.coste.syncorg.synchronizers;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by bcoste on 18/06/16.
 */
public class SyncResult {
    HashSet<String> newFiles;
    HashSet<String> changedFiles;
    Set<String> deletedFiles;
    State state;

    SyncResult(){
        newFiles = new HashSet<>();
        changedFiles = new HashSet<>();
        deletedFiles = new HashSet<>();
        state = State.kFailed;
    }

    void setState(State state){
        this.state = state;
    }

    enum State {
        kSuccess,
        kFailed
    }
}
