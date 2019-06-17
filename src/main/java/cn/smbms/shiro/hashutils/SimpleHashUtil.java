package cn.smbms.shiro.hashutils;

import org.apache.shiro.crypto.hash.SimpleHash;
import org.apache.shiro.util.ByteSource;

public class SimpleHashUtil {

	public static Object getHashByString(String algorithmName, Object source, Object salt, int hashIterations) {
		return new SimpleHash(algorithmName, source, ByteSource.Util.bytes(salt), hashIterations);
	}

}
