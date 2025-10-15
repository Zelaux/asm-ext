package asmext.analytics;

public class ByteArrSeq {
    public int size;
    public byte[][] arr=new byte[4][];

    public void add(byte[] elem) {
        if (arr.length == size) {
            byte[][] newArr = new byte[(size * 3 + 1) >> 1][];
            System.arraycopy(arr,0,newArr,0,size);
            arr=newArr;
        }
        arr[size++]=elem;
    }

}
