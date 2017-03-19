dir="$(dirname "${BASH_SOURCE[0]}")"
root_dir=$dir/..

function prompt {
    echo $1 " (yes/no)"
    read yesOrNo
    if [ $yesOrNo != "yes" ]; then
        exit 1
    fi
    echo ""
}

function check_no_log_remaining {
    ok=1
    for f in $(find $root_dir -name "*.java"); do
        if [ "$(grep -E 'Log\.v' $f)" != "" ]; then
            echo "Found a remaining Log.v in $f"
            ok=0
        fi
    done
    if [ "$ok" != "1" ];then
        exit -1
    fi
}

function check_tag_has_been_created {
    version_name=$(sed -n -r 's/.*versionName \"(.*)\"/\1/p' $dir/../SyncOrg/build.gradle)
    last_git_tag=$(git tag | tail -n 1)

    if [ $last_git_tag !=  $version_name ]; then
        echo "Creating a git tag"
        cmd="git tag $version_name && git push origin tag $version_name"
        echo $cmd
        exec $cmd
    fi
}

prompt 'Have you updated the string "upgrade" that describe this version changes in strings.xml ?'
prompt 'Have you changed "versionName" and "versionCode" in build.gradle ?'
check_no_log_remaining
check_tag_has_been_created

