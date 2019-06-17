package cn.smbms.shiro.realms;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.util.ByteSource;
import org.springframework.beans.factory.annotation.Autowired;

import cn.smbms.pojo.User;
import cn.smbms.service.UserService;

public class ShiroRealm extends AuthorizingRealm {

	@Autowired
	private UserService userService;

	@Override
	protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
		return null;
	}

	@Override
	protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {

		UsernamePasswordToken userToken = (UsernamePasswordToken) token;
		User userLoginInfo = new User();
		userLoginInfo.setUserCode(userToken.getUsername());
		User user = userService.getUserByCode(userLoginInfo.getUserCode());

		AuthenticationInfo authcInfo = null;
		Object principal = null;
		try {
			principal = user.getUserCode();
		} catch (Exception e) {
			throw new UnknownAccountException();
		}
		ByteSource credentialsSalt = ByteSource.Util.bytes(user.getUserCode());
		Object credentials = user.getUserPassword();

		if (null == user) {
			return null;
		}

		authcInfo = new SimpleAuthenticationInfo(principal, user.getUserPassword(), credentialsSalt, this.getName());
		return authcInfo;
	}

}
