#! /usr/bin/env python

import sys

for line in sys.stdin:
	line = line.strip().split()
	user = line[0]

	for i in range(1, len(line)):
		for j in range(i + 1, len(line)):
			if line[i] < line[j]:
				if user < line[i]:
					print ('%s %s %s\t%s' % (user, line[i], line[j], user))
					print ('%s %s %s\t%s' % (line[i], user, line[j], user))
					print ('%s %s %s\t%s' % (line[j], user, line[i], user))

				elif user > line[j]:
					print ('%s %s %s\t%s' % (line[i], line[j], user, user))
					print ('%s %s %s\t%s' % (line[j], line[i], user, user))
					print ('%s %s %s\t%s' % (user, line[i], line[j], user))

				else:
					print ('%s %s %s\t%s' % (line[i], user, line[j], user))
					print ('%s %s %s\t%s' % (user, line[i], line[j], user))
					print ('%s %s %s\t%s' % (line[j], line[i], user, user))
			else:
				if user < line[j]:
					print ('%s %s %s\t%s' % (user, line[j], line[i], user))
					print ('%s %s %s\t%s' % (line[j], user, line[i], user))
					print ('%s %s %s\t%s' % (line[i], user, line[j], user))
				elif user > line[i]:
					print ('%s %s %s\t%s' % (line[j], line[i], user, user))
					print ('%s %s %s\t%s' % (line[i], line[j], user, user))
					print ('%s %s %s\t%s' % (user, line[j], line[i], user))
				else:
					print ('%s %s %s\t%s' % (line[j], user, line[i], user))
					print ('%s %s %s\t%s' % (user, line[j], line[i], user))
					print ('%s %s %s\t%s' % (line[i], line[j], user, user))
