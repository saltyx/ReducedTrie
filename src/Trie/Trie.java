package Trie;

import java.io.*;
import java.util.*;

/**
 * Created by shiyan on 2017/2/16.
 * Contact: shiyan233@hotmail.com
 *          saltyx.github.io
 */

public class Trie {

    private static int BC_UNIT_SIZE = 8; // INT + INT
    private static int UNIT_SIZE = 4;// INT
    private static int DEFAULT_SIZE = 4096 * 32;
    private static int DEFAULT_TAIL_SIZE = 4096;
    private static int END_FLAG = 1;
    private static int DEBUG_MODE = 0;

    private int [] base;
    private int [] check;
    private int [] tail;
    private List<Integer> [] lists;

    private int position;

    public Trie() {
        initial();
    }

    public Trie(int... size) {
        initial(size);
    }

    @SuppressWarnings("unchecked")
    public void load(String filename) throws IOException {

        base = check = tail = null;
        lists = null;
        position = 1;

        File bcFile = new File(filename+".bc");
        int size = (int) bcFile.length() / BC_UNIT_SIZE;
        base = new int[size];
        check = new int[size];
        lists = new List[size];

        DataInputStream bcDataInputStream = new DataInputStream(
                new BufferedInputStream(
                        new FileInputStream(bcFile)));
        for (int i = 0; i < size; i++) {
            base[i] = bcDataInputStream.readInt();
            check[i] = bcDataInputStream.readInt();
        }
        bcDataInputStream.close();

        File tailFile = new File(filename + ".tail");
        DataInputStream tailDataInputStream = new DataInputStream(
                new BufferedInputStream(
                        new FileInputStream(tailFile)));
        int tailSize = (int) tailFile.length() / UNIT_SIZE;
        tail = new int[tailSize];
        for (int i = 0; i < tailSize; i++) {
            tail[i] = tailDataInputStream.readInt();
            if (tail[i] != 0) {
                position = i;
            }
        }
        position++;
        tailDataInputStream.close();

        File indexFile = new File(filename + ".index");
        DataInputStream indexDataInputStream = new DataInputStream(
                new BufferedInputStream(
                        new FileInputStream(indexFile)));

        File listFile = new File(filename + ".list");
        DataInputStream listDataInputStream = new DataInputStream(
                new BufferedInputStream(
                        new FileInputStream(listFile)));

        int indexSize = (int) indexFile.length() / BC_UNIT_SIZE;
        for (int i = 0; i < indexSize; i++) {
            int index = indexDataInputStream.readInt();
            int num = indexDataInputStream.readInt();
            lists[index] = new LinkedList<>();
            for (int j = 0; j < num; j++) {
                lists[index].add(listDataInputStream.readInt());
            }
        }
        indexDataInputStream.close();
        listDataInputStream.close();
    }

    @SuppressWarnings("unchecked")
    private void initial(int... size) {
        switch (size.length) {
            case 0 :
                base = new int[DEFAULT_SIZE];
                check = new int[DEFAULT_SIZE];
                lists = new List[DEFAULT_SIZE];
                tail = new int[DEFAULT_TAIL_SIZE];
                break;
            case 1:
                base = new int[size[0]];
                check = new int[size[0]];
                lists = new List[size[0]];
                tail = new int[DEFAULT_TAIL_SIZE];
                break;
            case 2:
                base = new int[size[0]];
                check = new int[size[0]];
                lists = new List[size[0]];
                tail = new int[size[1]];
                break;
            default:
                if (DEBUG_MODE == 1) {
                    info("using default size");
                }
                base = new int[DEFAULT_SIZE];
                check = new int[DEFAULT_SIZE];
                lists = new List[DEFAULT_SIZE];
                tail = new int[DEFAULT_TAIL_SIZE];
                break;
        }

        base[1] = 1;
        check[1] = 1;
        position = 1;
    }

    public void save(String filename) throws IOException {

        DataOutputStream dataOutputStreamForBC = new DataOutputStream(
                new BufferedOutputStream(
                        new FileOutputStream(filename+".bc")));
        for (int i = 0; i < base.length; i++) {
            dataOutputStreamForBC.writeInt(base[i]);
            dataOutputStreamForBC.writeInt(check[i]);
        }
        dataOutputStreamForBC.close();

        DataOutputStream dataOutputStreamForTail = new DataOutputStream(
                new BufferedOutputStream(
                        new FileOutputStream(filename + ".tail")));
        for (int x: tail) {
            dataOutputStreamForTail.writeInt(x);
        }
        dataOutputStreamForTail.close();

        DataOutputStream dataOutputStreamForIndex = new DataOutputStream(
                new BufferedOutputStream(
                        new FileOutputStream(filename + ".index")));
        DataOutputStream dataOutputStreamForList =  new DataOutputStream(
                new BufferedOutputStream(
                        new FileOutputStream(filename + ".list")));

        for (int i = 0; i < lists.length; i++) {
            if (lists[i] != null) {
                dataOutputStreamForIndex.writeInt(i);
                dataOutputStreamForIndex.writeInt(lists[i].size());
                for (int x: lists[i]) {
                    dataOutputStreamForList.writeInt(x);
                }
            }
        }

        dataOutputStreamForList.close();
        dataOutputStreamForIndex.close();
    }

    public void build(List<String> list) {
        for (int i = 0; i < list.size(); i++) {
            int code = insert(list.get(i));
            switch (code) {
                case -1:
                    error("accessing the empty node");
                    return;
                case -2:
                    if (DEBUG_MODE == 1) {
                        info(list.get(i) + " #"+ String.valueOf(i) + " has already been in the dictionary");
                    }
                    break;
                default:
                    if (DEBUG_MODE == 1) {
                        info(list.get(i)+"#"+i);
                    }
                    break;
            }
        }
    }

    public int insert(String key) {
        int[] keyValue = string2IntArray(key);
        if (!search(keyValue)) {
            int pre = 1;
            for (int i=0; i<keyValue.length; i++) {
                if (base[pre] < 0) {
                    //if current node is an end-point ,then separate or create a new node
                    int oldBase = base[pre];
                    if (tail[-oldBase] == keyValue[i]) {
                        //create a new node
                        base[pre] = xCheck(keyValue[i]);

                        checkBC(base[pre] + keyValue[i]);
                        base[ base[pre]+keyValue[i] ] = oldBase;
                        check[ base[pre]+keyValue[i] ] = pre;
                        put(pre, keyValue[i]);
                        moveTail(-oldBase);
                        pre = base[pre] + keyValue[i];
                    } else {
                        //separate
                        List<Integer> list = new ArrayList<>();
                        list.add(tail[-oldBase]); list.add(keyValue[i]);
                        base[pre] = xCheck(list);
                        checkBC(base[pre] + tail[-oldBase]);
                        checkBC(base[pre] + keyValue[i]);
                        base[ base[pre]+tail[-oldBase] ] = oldBase;
                        base[ base[pre]+keyValue[i] ] = -position;
                        check[ base[pre]+tail[-oldBase] ] = check[ base[pre]+keyValue[i] ] = pre;
                        writeTail(keyValue, i+1);
                        put(pre, tail[-oldBase]);
                        put(pre, keyValue[i]);
                        moveTail(-oldBase);
                        break;// 2 new nodes
                    }
                } else if (base[pre] > 0) {
                    if (check[ base[pre] + keyValue[i] ] == 0) {
                        check[ base[pre] + keyValue[i] ] = pre;
                        base[ base[pre] + keyValue[i] ] = -position;
                        put(pre, keyValue[i]);
                        writeTail(keyValue, i + 1);
                        break;//a new node
                    } else if (check[ base[pre] + keyValue[i] ] == pre) {
                        //put(pre, keyValue[i]);
                        pre = base[pre] + keyValue[i];
                    } else {
                        processConflict(pre, check[ base[pre] + keyValue[i] ], keyValue[i]);
                        writeTail(keyValue, i + 1);
                        break;//a new node
                    }
                } else return -1;
            }
        } else {
            return -2;
        }
        return 0;
    }

    private int processConflict(int node1, int node2, int newNodeValue) {
        int node = (lists[node1].size()+1) < lists[node2].size() ? node1 : node2;
        int oldNodeBase = base[node];
        if (node == node1) {
            base[node] = xCheck(lists[node], newNodeValue);
        } else {
            base[node] = xCheck(lists[node]);
        }
        for (int i = 0; i < lists[node].size(); i++) {
            int oldNext = oldNodeBase + lists[node].get(i);
            int newNext = base[node] + lists[node].get(i);
            if (oldNext == node1) node1 = newNext;
            base[newNext] = base[oldNext];
            check[newNext] = node;
            if (base[oldNext] > 0) {
                for (int j = 0; j < lists[oldNext].size(); j++) {
                    check[ base[oldNext] + lists[oldNext].get(j) ] = newNext;
                    put(newNext, lists[oldNext].get(j));
                }
                lists[oldNext] = null;
            }
            base[oldNext] = 0; check[oldNext] = 0;
        }
        base[ base[node1] + newNodeValue ] = -position;
        check[ base[node1] + newNodeValue ] = node1;
        put(node1, newNodeValue);
        return node;
    }

    private void checkBC(int p) {
        if (p >= base.length) {
            extendBC();
        }
    }

    private void checkTail(int p) {
        if (p >= tail.length) {
            extendT();
        }
    }

    @SuppressWarnings("unchecked")
    private void extendBC() {
        int[] base1 = new int[base.length + DEFAULT_SIZE];
        int[] check1 = new int[check.length + DEFAULT_SIZE];
        List<Integer>[] lists1 = new List[lists.length + DEFAULT_SIZE];
        System.arraycopy(base, 0, base1, 0, base.length);
        System.arraycopy(check, 0, check1, 0, check.length);
        System.arraycopy(lists, 0, lists1, 0, lists.length);
        base = base1;
        check = check1;
        lists = lists1;
    }

    private void extendT() {
        int[] tail1 = new int[tail.length + DEFAULT_TAIL_SIZE];
        System.arraycopy(tail, 0, tail1, 0 ,tail.length);
        tail = tail1;
    }

    private boolean search(int[] key) {
        int pre = 1;
        for (int i = 0; i < key.length; i++) {
            if (base[pre] < 0) {
                return compareTail(-base[pre], i, key);
            } else if (base[pre] > 0) {
                checkBC(base[pre]+key[i]);
                if (check[ base[pre] + key[i] ] == pre) {
                    pre = base[pre] + key[i];
                } else {
                    return false;
                }
            } else return false;
        }
        return true;
    }

    public boolean search(String key) {
        int[] keyValue = string2IntArray(key);
        return search(keyValue);
    }

    private boolean compareTail(int start,int keyIndex, int[] key) {
        checkTail(start+key.length-keyIndex+1);
        for (int i = start, j=keyIndex; j < key.length ; i++,j++) {
            if (key[j] != tail[i]) {
                return false;
            }
        }
        return true;
    }

    private void moveTail(int start) {
        moveTail(start, 1);
    }

    private void moveTail(int start, int steps) {
        if (steps <= 0 || tail[start] == END_FLAG) return;
        int i= start + steps;
        for (;tail[i] != END_FLAG; i++) {
            tail[i - steps] = tail[i];
        }
        for (int j = 0; j < steps; j++,i--) {
            tail[i] = 0;
        }
        tail[i] = END_FLAG;
    }

    public boolean delete(String key) {
        int []keyValue = string2IntArray(key);
        int pre = 1;
        int index = -1;
        int tempVal;
        int next;
        do {
            index++;
            tempVal = keyValue[index];
            next = base[pre] + tempVal;
            if (check[next] != pre)  {
                return false;
            }
            if (base[next] < 0) break;
            pre = next;
        } while (true);
        if (tempVal == END_FLAG || compareTail(-base[next], index+1, keyValue)) {
            for (int i = 0; i < lists[pre].size(); i++) {
                if (lists[pre].get(i) == tempVal) {
                    lists[pre].remove(i);break;
                }
            }
            base[next] = 0; check[next] = 0;
            //info(String.format("%s next[%d] turn to 0",key, next));
            return true;
        }
        return false;
    }

    public List<String> findByPrefix(String prefix) {
        StringBuilder builder = new StringBuilder();
        char[] key = prefix.toCharArray();
        int pre = 1;
        int next = 1;
        int index = 0;
        char tempVal;

        while (index < key.length) {
            tempVal = key[index];
            next = base[pre] + tempVal;
            builder.append(tempVal);
            ++index;
            if (check[next] != pre) return null;
            if (base[next] < 0) break;
            pre = next;

        }

        List<String> list = new ArrayList<>();

        if (base[next] < 0) {
            int i = 0;
            while (index < key.length) {
                if (tail[-base[next] + i] == key[index]) {
                    builder.append((char) tail[-base[next]+i]);
                    ++i;
                    ++index;
                }
                else return null;
            }
            while (tail[-base[next]+i] != END_FLAG) {
                builder.append((char) tail[-base[next]+i]);
                ++i;
            }
            list.add(builder.toString());
            return list;
        } else {
            List<String> temp = new LinkedList<>();
            find(pre, new StringBuilder(), temp);
            for (String x: temp) {
                list.add(builder.toString() + x);
            }
            return list;
        }
    }

    private void find( int pre, StringBuilder builder, List<String> list) {
        int next;
        if (base[pre] < 0) {
            builder.append(readTail(-base[pre]));
            list.add(builder.toString());
            return;
        }
        for (int i = 0; i < lists[pre].size(); i++) {
            next = base[pre] + lists[pre].get(i);
            StringBuilder reserved = new StringBuilder(builder.toString());
            if (check[next] == pre) {
                if (lists[pre].get(i) == END_FLAG) {
                    find(next, builder, list);
                } else {
                    find(next, builder.append((char) lists[pre].get(i).intValue()), list);
                }
            }
            builder = reserved;
        }
    }

    private String readTail(int start) {
        StringBuilder builder = new StringBuilder();
        while (tail[start] != END_FLAG) {
            builder.append((char) tail[start++]);
        }
        return builder.toString();
    }

    private int xCheck(int x) {
        int q = 1;
        while (true) {
            if (check[q+x] != 0) {
                q++;
            } else {
                break;
            }
        }
        return q;
    }

    private int xCheck(List<Integer> list) {
        int q = 1;
        findQ:while (true) {
            for (int i = 0; i < list.size(); i++) {
                if (check[q+list.get(i)] != 0) {
                    q++;
                    continue findQ;
                }
            }
            break;
        }
        return q;
    }

    private int xCheck(List<Integer> list, int x) {
        int q = 1;
        findQ:while (true) {
            for (int i = 0; i < list.size(); i++) {
                if (check[q+list.get(i)] != 0 || check[q+x] != 0) {
                    q++;
                    continue findQ;
                }
            }
            break;
        }
        return q;
    }

    private void writeTail(int[] key, int start) {
        if (start >= key.length) return;
        checkTail(position+key.length-start+1);
        for (int i=position, j=start; j<key.length; position++,i++,j++) {
            tail[i] = key[j];
        }
    }

    @SuppressWarnings("unchecked")
    private void put(int key, int value) {
        if (lists[key] == null) {
            lists[key] = new LinkedList();
            lists[key].add(value);
        } else {
            lists[key].add(value);
        }
    }

    private int[] string2IntArray(String key) {
        if (key == null || key.length() == 0) return null;
        int[] result = new int[key.length()+1];
        for (int i=0; i<key.length(); i++) {
            result[i] = key.charAt(i);
        }
        result[key.length()] = END_FLAG;
        return result;
    }

    private void log(String key, Object value) {
        System.out.println(String.format("%s : %s",key, String.valueOf(value)));
    }

    private void error(String value) {
        System.err.println(String.format("[error] %s", value));
    }

    private void info(String value) {
        System.out.println(String.format("[info] %s", value));
    }
}
