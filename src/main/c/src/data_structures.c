#include <stdlib.h>
#include "data_structures.h"

bool insertAfter(DoublyLinkedList_t *list, Node_t *node, Node_t *newNode)
{
    if (node->next == NULL)
    {
        list->tail = newNode;
        newNode->next = NULL;
    }
    else
    {
        newNode->next = node->next;
        node->next->prev = newNode;
    }
    node->next = newNode;
    return true;
}

bool insertBefore(DoublyLinkedList_t *list, Node_t *node, Node_t *newNode)
{
    if (node->prev == NULL)
    {
        list->head = newNode;
        newNode->prev = NULL;
    }
    else
    {
        newNode->prev = node->prev;
        node->prev->next = newNode;
    }
    node->prev = newNode;
    return true;
}

Node_t *remove(DoublyLinkedList_t *list, Node_t *node)
{
    if (node->next == NULL)
    {
        list->tail = node->prev;
    }
    else
    {
        node->next->prev = node->prev;
    }
    
    if (node->prev == NULL)
    {
        list->head = node->next;
    }
    else
    {
        node->prev->next = node->next;
    }
    return node;
}

bool pushFront(DoublyLinkedList_t *list, Node_t *newNode)
{
    return insertBefore(list, list->head, newNode);
}

bool pushBack(DoublyLinkedList_t *list, Node_t *newNode)
{
    return insertAfter(list, list->tail, newNode);
}

Node_t *popFront(DoublyLinkedList_t *list)
{
    return remove(list, list->head);
}

Node_t *popBack(DoublyLinkedList_t *list)
{
    return remove(list, list->tail);
}

Node_t *peekFront(DoublyLinkedList_t *list)
{
    return list->head;
}

Node_t *peekBack(DoublyLinkedList_t *list)
{
    return list->tail;
}
