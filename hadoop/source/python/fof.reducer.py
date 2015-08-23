#! /usr/bin/env python

import sys

last_key = None
num = 0

for line in sys.stdin:
	key, value = line.strip().split('\t')
	key = key.split(' ')

	if last_key == key:
		num = num + 1
	else:
		last_key = key
		num = 1

	if num == 3:
		print ('%s %s %s' % (last_key[0], last_key[1], last_key[2]))

