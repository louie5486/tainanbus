package com.hantek.ttia.protocol.a1a4;

public class FileStruct {
	public static final short Length = 10;

	/**
	 * 檔案名稱前4 碼縮寫，如"APTS"
	 */
	public String name;

	/**
	 * 檔案版本，以日期為準，格式為yymmdd，如"100215"
	 */
	public String version;

	public FileStruct() {
		this.name = "";
		this.version = "";
	}

	/**
	 * 
	 * @param name
	 *            檔案名稱前4碼縮寫，如"APTS"
	 * @param version
	 *            檔案版本，以日期為準，格式為yymmdd，如"100215"
	 */
	public FileStruct(String name, String version) {
		this.name = name;
		this.version = version;
	}

	public byte[] getBytes() {
		byte[] bytes = new byte[Length];
		int index = 0;

		this.name = String.format("%4s", this.name);
		for (int i = 0; i < 4; i++) {
			try {
				bytes[index] = this.name.getBytes()[i];
			} catch (Exception e) {
			}
			index++;
		}

		this.version = String.format("%6s", this.version);
		for (int i = 0; i < 6; i++) {
			try {
				bytes[index] = this.version.getBytes()[i];
			} catch (Exception e) {
			}
			index++;
		}

		return bytes;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("<FileStruct>" + "\n");
		sb.append("name=" + this.name + " ");
		sb.append("version=" + this.version + "\n");
		sb.append("</FileStruct>" + "\n");
		return sb.toString();
	}

	public FileStruct clone() {
		FileStruct varCopy = new FileStruct();

		varCopy.name = this.name;
		varCopy.version = this.version;

		return varCopy;
	}
}
