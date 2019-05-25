#ifndef DATA_STRUCTURES_H
#define DATA_STRUCTURES_H

#include <stdbool.h>

typedef struct Node_struct Node_t;
typedef struct DoublyLinkedList_struct DoublyLinkedList_t;

struct Node_struct
{
    void *val;
    Node_t *next;
    Node_t *prev;
};

struct DoublyLinkedList_struct
{
    Node_t *head;
    Node_t *tail;
};

bool insertAfter(DoublyLinkedList_t *list, Node_t *node, Node_t *newNode);
bool insertBefore(DoublyLinkedList_t *list, Node_t *node, Node_t *newNode);
Node_t *remove(DoublyLinkedList_t *list, Node_t *node);

bool pushFront(DoublyLinkedList_t *list, Node_t *newNode);
bool pushBack(DoublyLinkedList_t *list, Node_t *newNode);
Node_t *popFront(DoublyLinkedList_t *list);
Node_t *popBack(DoublyLinkedList_t *list);
Node_t *peekFront(DoublyLinkedList_t *list);
Node_t *peekBack(DoublyLinkedList_t *list);

#endif