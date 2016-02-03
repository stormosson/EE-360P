#!/usr/bin/env python
# Written by Eric and Stormy
# 2016-02-02

import math


class Thing:

    def __init__(self, name, priority):
        self.name = name
        self.priority = priority


    def __str__(self):
        return "[%s, %s]" % (self.name, self.priority)


def parent_of(index):
    parent = math.floor((index-1)/2)
    if parent < 0:
        raise Exception()
    print("Parent of {} is {}".format(index, parent))
    return parent


class PriorityQueue:

    # Thing[] things


    def __init__(self):
        self.things = []


    def add(self, name, priority):
        # sync: indicate 'in-writing'
        if self.search(name) != -1:
            return -1
        # spwan child thread
        t = Thing(name, priority)
        self.things.append(t)
        self.filter_up()
        for i in self.things:
            print(i)


    def search(self, name):
        return -1


    def poll(self):
        pass


    def filter_up(self):
        print("Filtering up the %s" % self.things[-1])
        try:
            target_index = len(self.things)-1
            while(self.things[target_index].priority < self.things[parent_of(target_index)].priority):
                parent_of_target_index = parent_of(target_index)
                self.swap(target_index, parent_of_target_index)
                target_index = parent_of_target_index
        except:
            pass

    def swap(self, a, b):
        print("Swapping %s and %s" % (a, b))
        self.things[a], self.things[b] = self.things[b], self.things[a]


q = PriorityQueue()
for i in [1,3,6,5,8,2,4,0]:
    q.add(i, i)
    print("Added a certain %s" % i)
