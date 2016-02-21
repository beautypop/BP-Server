#!/bin/bash


FTP_HOME=/home/ftp


# remove all version zips except most recent four
cd $FTP_HOME

#server
ls -rt | grep zip | grep bp-server | head -n -4 | while read f; do
  rm -f "$f"
done

#admin
#ls -rt | grep zip | grep bp-admin | head -n -4 | while read f; do
#  rm -f "$f"
#done

echo "Done purging old bp binary zips from $FTP_HOME"