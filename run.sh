#!/usr/bin/env bash

cd src

javac LogParser.java


java LogParser ./../log_input/log.txt 10

cd ..

