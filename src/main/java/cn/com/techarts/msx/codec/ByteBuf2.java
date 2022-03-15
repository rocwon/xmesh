package cn.com.techarts.msx.codec;

public final class ByteBuf2 {
	private byte[] bytes;
	private int capacity;
	private int actualLength;
	
	public ByteBuf2(int capacity) {
		bytes = new byte[capacity];
		this.capacity = capacity;
	}
	
	public void put(byte[] array) {
		if(array == null) return;
		if(array.length == 0) return;
		actualLength = array.length;
		if(actualLength > capacity) {
			bytes = new byte[actualLength];
		}
		System.arraycopy(array, 0, bytes, 0, actualLength);
	}
	
	public byte[] toBytes() {
		byte[] result = new byte[actualLength];
		System.arraycopy(bytes, 0, result, 0, actualLength);
		return result;
	}
}