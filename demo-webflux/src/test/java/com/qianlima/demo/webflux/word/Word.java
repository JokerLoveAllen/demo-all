package com.qianlima.demo.webflux.word;

import java.util.*;
/**
 * @ClassName Word
 * @Author lqs
 * @Date 2020/5/25 11:55
 * base idea:
 * success search word with length ${2:6}
 * Than: LRU + Trie
 */
public class Word {

}

class TrieNode extends LRUNode{
    //是否为根节点
    boolean isRoot;
    //是否为叶子节点
    boolean isLeaf;
    //是否为单词
    boolean isWord;
    //当前最大的调用次数
    long maxCount;
    //当前最大的调用值
    String maxValue;
    //当前值
    char chr;
    //父节点
    TrieNode par;
    //子节点
    Map<Character,TrieNode> childs;
    //当前节点进行LRU的数据结构相关
    int transhold = 15;
    LRUNode dummyHead;
    LRUNode dummyTail;

    public TrieNode(){init();}

    private void init(){
        if(!isLeaf){
            dummyHead = new LRUNode();
            dummyTail = new LRUNode();
            dummyHead.next = dummyTail;
            dummyTail.prev = dummyHead;
        }
    }

    void add(TrieNode node){
        if(childs.size() > transhold - 1){
            TrieNode tmp = (TrieNode)dummyTail.prev;
            tmp.prev.next = dummyTail;
            dummyTail.prev = tmp.prev;
            tmp.prev = tmp.next = null;

            this.childs.remove(tmp.chr);
        }else if(!this.childs.containsKey(node.chr)){
            this.childs.put(node.chr,node);
        }
        LRUNode tmp = dummyHead.next;
        dummyHead.next = node;
        node.next = tmp;
        node.prev = dummyHead;
        tmp.prev = node;
        if(node.isWord){
            node.maxCount++;
            // bottom-up maintain maxCount
            while(node.par != null){
                TrieNode par = node.par;
                par.maxCount = Math.max(par.maxCount,node.maxCount);
                node = par;
            }
        }
    }
}

class LRUNode{
    LRUNode next, prev;
}