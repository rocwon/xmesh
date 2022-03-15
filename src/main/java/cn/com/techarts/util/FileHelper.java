/**
 * 
 */
package cn.com.techarts.util;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;

import org.apache.log4j.Logger;

public class FileHelper
{
	private static final int DEF_BUFFER_SIZE = 4096;
	static private final Logger log = Logger.getLogger(FileHelper.class);
	
	public static void cp( String src, String dest)
	{
		try{
			var fis = new FileInputStream(src);
			var fos = new FileOutputStream(dest); 
			var fic = fis.getChannel();
			var foc = fos.getChannel();
			var buf = ByteBuffer.allocateDirect( DEF_BUFFER_SIZE);
			int offset = 0;
			while( offset != -1){
				buf.clear();
				offset = fic.read( buf);
				buf.flip();
				foc.write( buf);
			}
			foc.close();
			fic.close();
			fis.close();
			fos.close();
		}catch( IOException e){
			log.error( "Error copying file.", e);
		}
	}
	/**
	 * Write file content to a given output stream such as ServletOutputStream.
	 * */
	public static void write( String src, OutputStream dest)
	{
		if( src == null || dest == null) return;
		try{
			var fis = new FileInputStream( src);
			write(fis, dest);
			if(fis != null) fis.close();
			
		}catch( IOException e){ e.printStackTrace();}
	}
	
	public static void writeString( String content, OutputStream dest)
	{
		if( content == null || dest == null) return;
		try{
			var in = new ByteArrayInputStream( content.getBytes("gbk"));
			write( in, dest);
		}catch( IOException e){ e.printStackTrace(); }
	}
	
	public static void write( InputStream in, OutputStream dest)
	{
		if( in == null || dest == null) return;
		var buffer = new byte[DEF_BUFFER_SIZE];
		int offset = 0;
		try{
			while( offset != -1){
				offset = in.read( buffer);
				dest.write( buffer);
			}
			dest.flush();
			dest.close();
			in.close();
		}catch( IOException e){ e.printStackTrace(); }
	}
	
	/**
	 * Generate a temp file path.
	 * */
	public static String ftmp( String fileName)
	{
		var tmpDir = System.getProperty( "java.io.tmpdir");
		var separator = System.getProperty( "file.separator");
		if( tmpDir == null) tmpDir = System.getProperty("user.dir");
		if( !tmpDir.endsWith( separator)){
			tmpDir = new StringBuilder(tmpDir).append( separator).toString();
		}
		return new StringBuilder(64).append(tmpDir).append( fileName).toString();
	}
	
	public static void create(byte[] content, String name) {
		if( content == null || name == null) return;
		try{
			var in = new ByteArrayInputStream(content);
			File file = new File(name);
			if(!file.exists()) file.createNewFile();
			write( in, new FileOutputStream(file));
		}catch( IOException e){ e.printStackTrace(); }
		
	}
	
	public static void mv( String src, String dest)
	{
		cp( src, dest);
		rm( src);
	}
	
	public static void rm( String path)
	{
		var file = new File( path);
		rm( file);
	}
	
	public static void rm( File file)
	{
		if( file != null) file.delete();
	}
	
	public static File mkdir( String rootPath, String userName)
	{
		File result = null;
		char[] user = userName.toCharArray();
		if( user != null && user.length > 0){
			var path = new StringBuffer( rootPath);
			for( int i = 0; i < user.length; i++){
				path.append( '/').append( user[i]);
			}
			result = new File( path.toString());
			if( !result.exists() || !result.isDirectory()){
				result.mkdirs();
			}
		}
		return result;
	}
	
	public static File[] poll( String srcFolder, String fileType)
	{
		var directory = new File( srcFolder);
		return directory.listFiles( new XFileFilter( fileType));
	}
	
	public static boolean exists(String path){
		var file = new File(path);
		return file.exists();
	}
	
	public static boolean classExists(String path){
		try {
			return Class.forName( path) != null;
		} catch (ClassNotFoundException e) {
			return false;
		}
	}	
}

class XFileFilter implements FileFilter
{
	private String type = null;
	
	public XFileFilter( String fileType)
	{
		this.type = fileType;
	}
	
	@Override
	public boolean accept( File file)
	{
		if( file == null) return false;
		if( this.type == null || this.type.isEmpty()) return true;
		return file.isFile() && file.getName().endsWith( this.type);
	}
}