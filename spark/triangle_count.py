"""
Get the list of cycle triangles in the graph
"""

from pyspark import SparkContext
from time import time
import sys, os

#TODO: Possibly define functions up here

def parse_line(line):
    line = line.split()
    leading_friend = int(line[0])
    adj_list = map(lambda arg: int(arg), line[1:])
    return (leading_friend, filter(lambda arg: arg < leading_friend, adj_list))

def intermediate_lists(friend_list):
    leading_friend = friend_list[0]
    friend_list = friend_list[1]
    return map(lambda arg: (arg, (leading_friend,  filter( lambda friend: friend < arg, friend_list ) )), friend_list)

def forms_triads(friend_list):
    user1 = friend_list[0]
    user2 = friend_list[1][1][0]
    list1 = friend_list[1][1][1]
    list2 = friend_list[1][0]
    common_friends = set(list1).intersection(set(list2))
    return map(lambda arg: sorted([user1, user2, arg], reverse=True), common_friends)



# NOTE: Do not change the name/signature of this function
def count_triangles(data, master="local[2]"):
    """
    @brief: Count triangles using Spark
    @param data: The data location for the input files
    @param master: The master URL as defined at
        https://spark.apache.org/docs/1.1.0/submitting-applications.html#master-urls
    """

    #################  NO EDITS HERE ###################
    assert not os.path.exists("triangles.out"), "File: triangles.out \
            already exists"
    sc = SparkContext(master, "Triangle Count")
    start = time()
    ###############  END NO EDITS HERE  ################

    # TODO: Your code goes here!

    files = sc.textFile(data).coalesce(4)

    l1 = files.map(parse_line).filter(lambda arg: arg[1] != [])
    l1.cache()
    l2 = l1.flatMap(intermediate_lists).filter(lambda arg: arg[1][1] != [])
    output = l1.join(l2).flatMap(forms_triads).collect()



    #################  NO EDITS HERE  ###################
    print "\n\n*****************************************"
    print "\nTotal algorithm time: %.4f sec \n" % (time()-start)
    print "*****************************************\n\n""" 
    ###############  END NO EDITS HERE ################

    with open("triangles.out", "wb") as f:
        # f.write("") # TODO: Loop with f to write your result to file serially
        # pass
        for op in output:
            line = ' '.join(map(lambda arg: str(arg), op))
            f.write(line + "\n")


#################  NO EDITS HERE  ###################
if __name__ == "__main__":
    if len(sys.argv) == 2:
        print "Counting triangles with master as 'local[2]'"
        count_triangles(sys.argv[1])
    elif len(sys.argv) == 3: 
        print "Counting triangles with master as '%s'" % sys.argv[2]
        count_triangles(sys.argv[1], sys.argv[2])
    else:
        sys.stderr.write("\nusage: SPARK_ROOT/bin/spark-submit \
            example/python/tri_count.py data_dir [master-url]")
        exit(1)
############### NO EDITS BELOW EITHER ################