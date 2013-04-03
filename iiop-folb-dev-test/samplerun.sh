#!/bin/bash -x
# sample local test execution script
source ./setup.sh
#./run.sh 
./run.sh -include "lbfail,15637,14755"
#./run.sh -exclude "15804sfsb,15804sfsb_kill,15804sfsb_kill_delete"

