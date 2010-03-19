#!/bin/bash

rm -rf dir-with-10files dir-with-9files dir-with-mixedfiles sourcefiles testfiles ~/testfiles
# generate 10MB file
dd if=/dev/zero of=test10MB.bin bs=10485760 count=1

# generate 1MB files
dd if=/dev/zero of=test01MB-0.bin bs=1048576 count=1
dd if=/dev/zero of=test01MB-1.bin bs=1048576 count=1
dd if=/dev/zero of=test01MB-2.bin bs=1048576 count=1
dd if=/dev/zero of=test01MB-3.bin bs=1048576 count=1
dd if=/dev/zero of=test01MB-4.bin bs=1048576 count=1
dd if=/dev/zero of=test01MB-5.bin bs=1048576 count=1
dd if=/dev/zero of=test01MB-6.bin bs=1048576 count=1
dd if=/dev/zero of=test01MB-7.bin bs=1048576 count=1
dd if=/dev/zero of=test01MB-8.bin bs=1048576 count=1
dd if=/dev/zero of=test01MB-9.bin bs=1048576 count=1

mkdir dir-with-10files
mkdir dir-with-9files
mkdir dir-with-mixedfiles
mkdir sourcefiles

cp test01MB-*.bin dir-with-10files/
cp test01MB-*.bin dir-with-9files/
rm dir-with-9files/test01MB-9.bin
cp *.bin dir-with-mixedfiles/
mv *.bin sourcefiles/

mkdir testfiles
mv dir-with-10files dir-with-9files dir-with-mixedfiles sourcefiles testfiles
mv testfiles ~/

