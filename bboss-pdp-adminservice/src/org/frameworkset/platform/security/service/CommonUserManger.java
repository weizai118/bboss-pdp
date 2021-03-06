package org.frameworkset.platform.security.service;

import com.frameworkset.orm.transaction.TransactionManager;
import com.frameworkset.platform.admin.entity.SmOrganization;
import com.frameworkset.platform.admin.entity.SmUser;
import com.frameworkset.platform.admin.service.SmOrganizationService;
import com.frameworkset.platform.admin.service.SmUserService;
import com.frameworkset.platform.admin.service.UserOrgParamManager;
import com.frameworkset.util.StringUtil;
import org.frameworkset.event.Event;
import org.frameworkset.event.EventHandle;
import org.frameworkset.event.EventImpl;
import org.frameworkset.platform.config.ConfigManager;
import org.frameworkset.platform.security.AccessControl;
import org.frameworkset.platform.security.event.ACLEventType;
import org.frameworkset.platform.security.service.entity.Result;
import org.frameworkset.platform.util.EventUtil;
import org.frameworkset.platform.util.LogManagerInf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.sql.Timestamp;

public class CommonUserManger implements CommonUserManagerInf{
	private static Logger log = LoggerFactory.getLogger(CommonUserManger.class);
//	private ConfigSQLExecutor executor ;
	private SmUserService userService;
	private SmOrganizationService organizationService;
	private UserOrgParamManager userOrgParamManager = new UserOrgParamManager();
	public CommonUserManger() {
		// TODO Auto-generated constructor stub
	}
	@Override
	public Result getUserById(String userId)
	{
		Result result = new Result();
		try {
			SmUser user = userService.getSmUser(userId);
//			CommonUser user = executor.queryObject(CommonUser.class,"getuserbyuserid", user_id);
			result.setCode(Result.ok);
			result.setUser(user);
		} catch (Exception e) {
			result.setCode(Result.fail);
			String m = new StringBuilder().append("通过用户id获取用户").append(userId).append("失败:").append(e.getMessage()).toString();
			result.setErrormessage(m);
			log.error(m,e);
		}
		return result;
	}
	@Override
	public Result getUserByWorknumber(String userWorknumber)
	{
		Result result = new Result();
		try {


			SmUser user = userService.getUserByWorknumber(userWorknumber);
			result.setCode(Result.ok);
			result.setUser(user);
		} catch (Exception e) {
			result.setCode(Result.fail);
			String m = new StringBuilder().append("通过工号获取用户").append(userWorknumber).append("失败:").append(e.getMessage()).toString();
			result.setErrormessage(m);
			log.error(m,e);
		}
		return result;
	}
	@Override
	public Result getUserByUserAccount(String userAccount)
	{
		Result result = new Result();
		try {



			SmUser user = userService.getSmUserByName(userAccount);
			result.setCode(Result.ok);
			result.setUser(user);
		} catch (Exception e) {
			result.setCode(Result.fail);
			String m = new StringBuilder().append("通过账号获取用户").append(userAccount).append("失败:").append(e.getMessage()).toString();
			result.setErrormessage(m);
			log.error(m,e);
		}
		return result;
	}

	public boolean exist(String userAccount) throws Exception
	{
		return userService.checkuserexist(userAccount) ;
	}
	@Override
	public Result createUser(SmUser user) {
		Result result = new Result();
		TransactionManager tm = new TransactionManager();
		try {
			tm.begin();
			if(exist(user.getUserName()))
			{
				result.setCode(Result.fail);
				result.setErrormessage(new StringBuilder().append("用户").append(user.getUserName()).append("已经存在.").toString());
			}

			user.setUserIsvalid(2);
			user.setUserType("0");

			user.setUserRegdate(new Timestamp(System.currentTimeMillis()));

			this.userService.addSmUser(user);
			result.setCode(Result.ok);
			result.setUser(user);
			tm.commit();
			 AccessControl control = AccessControl.getAccessControl();
				String operContent="";
		        String operSource=control.getMachinedID();
		        String openModle="用户管理";
		        String userName = control.isGuest()?"通用用户部门管理服务":control.getUserName();
		        String description="";
				LogManagerInf logManager = ConfigManager.getInstance().getLogManager();
				operContent=new StringBuilder().append(userName).append("创建用户：").append(user.getUserName()).append("(").append(user.getUserRealname()).append(")").toString();
				 description="";
		        logManager.log(control.getUserAccount() ,operContent,openModle,operSource,description);
		} catch (Exception e) {
			result.setCode(Result.fail);
			String m = new StringBuilder().append("创建用户").append(user.getUserName()).append("失败:").append(e.getMessage()).toString();
			result.setErrormessage(m);
			log.error(m,e);
			AccessControl control = AccessControl.getAccessControl();
			String operContent="";
			String operSource=control.getMachinedID();
			String openModle="用户管理";
			String userName = control.isGuest()?"通用用户部门管理服务":control.getUserName();
			String description="";
			LogManagerInf logManager = ConfigManager.getInstance().getLogManager();
			operContent=m;
			description="";
			logManager.log(control.getUserAccount() ,operContent,openModle,operSource,description);
		}
		finally
		{
			tm.release();
		}
		return result;
	}
	public Result addOrganization(SmOrganization org)
	{
		return addOrganizationWithEventTrigger(org,true);
	}

	/**
	 *  常用字段：
 *
 * orgId,
 * orgName,
 * parentId,
 * code,
 * creatingtime,
 * orgnumber,
 * orgdesc,
 * remark5, 显示名称
 * orgTreeLevel,部门层级，自动运算
 * orgleader 部门主管
 * 如果triggerEvent为false，需要调用程序自己触发以下事件
 * if(triggerEvent)
			{
				EventUtil.sendORGUNIT_INFO_ADD(org.getOrgId());
			}
	 * @param org
	 * @return
	 */
	public Result addOrganizationWithEventTrigger(SmOrganization org,boolean triggerEvent) {
		Result result = new Result();
		TransactionManager tm = new TransactionManager();
		try {
			tm.begin();
			if(this.organizationService.existOrgCode(org.getCode()))
			{
				result.setCode(Result.fail);
				result.setErrormessage(new StringBuilder().append("机构编码").append(org.getCode()).append("已经存在.").toString());
				return result;
			}

			this.organizationService.addSmOrganization(org);

			result.setCode(Result.ok);
			result.setOrg(org);
			tm.commit();
//			if(triggerEvent)
//			{
//				EventUtil.sendORGUNIT_INFO_ADD(org.getOrgId());
//			}
			 AccessControl control = AccessControl.getAccessControl();
			String operContent="";
	        String operSource=control.getMachinedID();
	        String openModle="部门管理";
	        String userName = control.isGuest()?"通用用户部门管理服务":control.getUserName();
	        String description="";
			LogManagerInf logManager = ConfigManager.getInstance().getLogManager();
			operContent=new StringBuilder().append(userName).append("创建部门：").append(org.getOrgName()).append("(").append(org.getOrgId()).append(")").toString();
			 description="";
	        logManager.log(control.getUserAccount() ,operContent,openModle,operSource,description);


		} catch (Exception e) {
			result.setCode(Result.fail);
			String m = new StringBuilder().append("创建机构").append(org.getOrgName()).append("失败:").append(e.getMessage()).toString();
			result.setErrormessage(m);
			log.error(m,e);
			AccessControl control = AccessControl.getAccessControl();
			String operContent="";
			String operSource=control.getMachinedID();
			String openModle="部门管理";
			String userName = control.isGuest()?"通用用户部门管理服务":control.getUserName();
			String description="";
			LogManagerInf logManager = ConfigManager.getInstance().getLogManager();
			operContent=m;
			description="";
			logManager.log(control.getUserAccount() ,operContent,openModle,operSource,description);
		}
		finally
		{
			tm.release();
		}
		return result;
	}

	private boolean existOrg(String orgId) throws SQLException {
		return this.organizationService.existOrg(orgId);
	}
	@Override
	public Result createTempUser(SmUser user) {
		Result result = new Result();
		TransactionManager tm = new TransactionManager();
		try {
			tm.begin();
			if(exist(user.getUserName()))
			{
				result.setCode(Result.fail);
				result.setErrormessage(new StringBuilder().append("用户").append(user.getUserName()).append("已经存在.").toString());
			}

			user.setUserIsvalid(1);
			user.setUserType("0");

			user.setUserRegdate(new Timestamp(System.currentTimeMillis()));
			this.userService.addSmUser(user);
			result.setCode(Result.ok);
			result.setUser(user);
			tm.commit();
			AccessControl control = AccessControl.getAccessControl();
			String operContent="";
			String operSource=control.getMachinedID();
			String openModle="用户管理";
			String userName = control.isGuest()?"通用用户部门管理服务":control.getUserName();
			String description="";
			LogManagerInf logManager = ConfigManager.getInstance().getLogManager();
			operContent=new StringBuilder().append(userName).append("创建临时用户：").append(user.getUserName()).append("(").append(user.getUserRealname()).append(")").toString();
			description="";
			logManager.log(control.getUserAccount() ,operContent,openModle,operSource,description);
		} catch (Exception e) {
			result.setCode(Result.fail);
			String m = new StringBuilder().append("创建临时用户").append(user.getUserName()).append("失败:").append(e.getMessage()).toString();
			result.setErrormessage(m);
			AccessControl control = AccessControl.getAccessControl();
			String operContent="";
			String operSource=control.getMachinedID();
			String openModle="用户管理";
			String userName = control.isGuest()?"通用用户部门管理服务":control.getUserName();
			String description="";
			LogManagerInf logManager = ConfigManager.getInstance().getLogManager();
			operContent=m;
			description="";
			logManager.log(control.getUserAccount() ,operContent,openModle,operSource,description);
			log.error(m,e);
		}
		finally
		{
			tm.release();
		}
		return result;
	}
	@Override
	public Result buildUserOrgRelation(String userid,String orgid)
	{
		return buildUserOrgRelationWithEventTrigger(userid,orgid,true);
	}
	@Override
	public Result buildUserOrgRelationWithEventTrigger(String userid,String orgid,boolean broadcastevent) {
		Result result = new Result();
		TransactionManager tm = new TransactionManager();
		try {
			tm.begin();
			if(existJobReleation(userid,  orgid))
			{

				result.setErrormessage(new StringBuilder().append("用户机构岗位关系[user=").append(userid).append(",org=").append(orgid).append("]已经存在.").toString());
			}
			else{
				this.organizationService.buildUserOrgRelationWithEventTrigger(userid,orgid);
			}



			result.setCode(Result.ok);

			tm.commit();
			if(broadcastevent)
				EventUtil.sendUSER_ROLE_INFO_CHANGEEvent(userid+"");
		} catch (Exception e) {
			result.setCode(Result.fail);
			String m = new StringBuilder().append("构建用户机构关系[user=").append(userid).append(",org=").append(orgid).append("]失败.").toString();
			result.setErrormessage(m);
			log.error(m,e);
		}
		finally
		{
			tm.release();
		}
		return result;
	}



	private boolean existJobReleation(String userid, String orgid) throws SQLException {

		return this.organizationService.existJobReleation(userid, orgid);
	}
//	private boolean existOrgReleation(String userid) throws SQLException {
//
//		return executor.queryObject(int.class, "existOrgReleation",  userid) > 0;
//	}
	public Result updateUser(SmUser user) {
		Result result = new Result();
		try {

//			if(exist(user.getUser_name()))
//			{
//				result.setCode(Result.fail);
//				result.setErrormessage(new StringBuilder().append("用户").append(user.getUser_name()).append("已经存在.").toString());
//			}
//			user.setUpdate_time(new Date());
//			executor.updateBean("updatecommonuser", user);
			this.userService.updateSmUser(user);
			result.setCode(Result.ok);
			result.setUser(user);
		} catch (Exception e) {
			result.setCode(Result.fail);
			String m = new StringBuilder().append("更新用户").append(user.getUserName()).append("失败:").append(e.getMessage()).toString();
			result.setErrormessage(m);
			log.error(m,e);
		}
		return result;
	}
	@Override
	public Result  updatePassword(String user_id,String password,String newPasswordSecond,String oldPassword)
	{
		Result result = new Result();
		try {
			this.userService.modifypassword(user_id,password, newPasswordSecond,oldPassword);
			result.setCode(Result.ok);
			result.setOperationData(""+user_id);
		} catch (Exception e) {
			result.setCode(Result.fail);
			String m = new StringBuilder().append("更新用户").append(user_id).append("口令失败:").append(e.getMessage()).toString();
			result.setErrormessage(m);
			log.error(m,e);
		}
		return result;
	}

	@Override
	public Result  deleteUser(String useraccount) {
		return _changeUserStatus(useraccount,0,0,"删除");
	}

	@Override
	public Result  deleteUserByID(String userid) {
		return _changeUserStatus(userid,2,0,"删除");
	}

	private void _upatestatus(String user_id,int status) throws Exception
	{
		this.userService.updateUserStatus(user_id,status +"");
	}

	@Override
	public Result deleteUserByWorknumber(String worknumber) {

		return _changeUserStatus(worknumber,1,0,"删除");

	}

	private Result _changeUserStatus(String user_id,int type,int status,String message)
	{
		Result  user = null;
		TransactionManager tm = new TransactionManager();
		try
		{
			tm.begin();
			if(type == 0)
				user = this.getUserByUserAccount((String)user_id);
			else if(type == 1)
				user = this.getUserByWorknumber((String)user_id);
			else
				user = this.getUserById(user_id);
			if(user.getCode().equals(user.ok))
			{
				_upatestatus(user.getUser().getUserId(),status);
				user = new Result();
				user.setCode(user.ok);
				user.setOperationData(new StringBuilder().append(message).append("用户").append(user_id).append("成功.").toString());
			}
			tm.commit();
		}
		catch(Exception e)
		{
			user = new Result();
			user.setCode(Result.fail);
			String m = new StringBuilder().append(message).append("用户").append(user_id).append("失败:").append(e.getMessage()).toString();
			user.setErrormessage(m);
			log.error(m,e);
		}
		finally
		{
			tm.release();
		}
		return user;
	}
	@Override
	public Result disableUser(String useraccount) {
		return _changeUserStatus(useraccount,0,3,"通过账号禁用");
	}

	@Override
	public Result disableUserByID(String userid) {
		return _changeUserStatus(userid,2,3,"通过账号ID禁用");
	}

	@Override
	public Result disableUserByWorknumber(String worknumber) {
		return _changeUserStatus(worknumber,1,3,"通过工号禁用");
	}


	@Override
	public Result openUser(String useraccount) {
		return _changeUserStatus(useraccount,0,2,"开通");

	}

	@Override
	public Result openUserByID(String userid) {
		return _changeUserStatus(userid,2,2,"开通");
	}

	@Override
	public Result openUserByWorknumber(String worknumber) {
		return _changeUserStatus(worknumber,1,2,"开通");
	}







	@Override
	public Result updateOrganization(SmOrganization org,  boolean broadcastevent) {
		Result result = new Result();
		if(StringUtil.isEmpty(org.getOrgId()))
		{


			result.setCode(result.fail);
			result.setErrormessage("部门ID为空");
			return result;
		}


		boolean tag = true;

		String notice = "修改部门失败";


		try {
			 this.organizationService.updateSmOrganization(org);

			// 修改机构重新加载缓冲
			// --记日志--------------------------------
			AccessControl control = AccessControl.getAccessControl();
			String operContent = "";
			String operSource = control.getMachinedID();
			String openModle ="部门管理";
			String userName = control.getUserName();
			String description = "";
			LogManagerInf logManager = ConfigManager.getInstance().getLogManager();
			operContent = userName + "修改"+ org.getOrgName();
			description = "";
			logManager.log(control.getUserAccount(), operContent, openModle, operSource, description);


				result.setErrormessage("部门修改成功!");





			result.setCode(result.ok);
			 EventUtil.sendORGUNIT_INFO_UPDATE(org.getOrgId());

		} catch (Exception e) {
			result.setCode(result.fail);
			String error = StringUtil.exceptionToString(e);
			result.setErrormessage(error);
			AccessControl control = AccessControl.getAccessControl();
			String operContent = "";
			String operSource = control.getMachinedID();
			String openModle ="部门管理";
			String userName = control.getUserName();
			String description = "";
			LogManagerInf logManager = ConfigManager.getInstance().getLogManager();
			operContent = userName + "修改"+ org.getOrgName()+"失败："+error;
			description = "";
			logManager.log(control.getUserAccount(), operContent, openModle, operSource, description);


		}

		return result;
	}
	@Override
	public Result disableOrganization(String orgid,  boolean broadcastevent) {
		Result result = new Result();
		if(StringUtil.isEmpty(orgid))
		{


			result.setCode(result.fail);
			result.setErrormessage("部门ID为空");
			return result;
		}

		try {
			_updateOrganizationStatus(orgid, "0");
			result.setCode(result.ok);
			result.setErrormessage("禁用部门成功！");
			if(broadcastevent)
			{
				EventUtil.sendORGUNIT_INFO_UPDATE(orgid);
			}
		} catch (Exception e) {
			result.setCode(result.fail);
			result.setErrormessage(StringUtil.exceptionToString(e));
		}
		return result;
	}

	@Override
	public Result enableOrganization(String orgid,  boolean broadcastevent) {
		Result result = new Result();
		if(StringUtil.isEmpty(orgid))
		{


			result.setCode(result.fail);
			result.setErrormessage("部门ID为空");
			return result;
		}

		try {
			_updateOrganizationStatus(orgid, "1");
			result.setCode(result.ok);
			result.setErrormessage("启用部门成功！");
			if(broadcastevent)
			{
				EventUtil.sendORGUNIT_INFO_UPDATE(orgid);
			}
		} catch (Exception e) {
			result.setCode(result.fail);
			result.setErrormessage(StringUtil.exceptionToString(e));
		}
		return result;
	}

	private void _updateOrganizationStatus(String orgid, String status )
	{
		this.organizationService.updateOrganizationStatus(orgid, status );



	}
	@Override
	public Result deleteOrganization(String orgid, boolean triggerEvent) {

		Result result = new Result();
		if(StringUtil.isEmpty(orgid))
		{


			result.setCode(result.fail);
			result.setErrormessage("部门ID为空");
			return result;
		}



		TransactionManager tm = new TransactionManager();

		try {
			tm.begin();


			StringBuilder hasnosonOrgs = new StringBuilder();
			StringBuilder hassonOrgs = new StringBuilder();
			//帅选出可以删除的部门
			int i = 0;
			boolean hasManagers = false;


			if(!organizationService.hasSon(orgid)){

					hasnosonOrgs.append(orgid);
					if(organizationService.hasManager(orgid))
						hasManagers = true;
			}
			else
			{

					hassonOrgs.append(orgid);

			}


			if(hasnosonOrgs.length() > 0) {
				organizationService.deleteBatchSmOrganization(hasnosonOrgs.toString().split(","));
				result.setCode(result.ok);
				result.setErrormessage("删除部门成功！");
			}
			else {
				if (hassonOrgs.length() > 0)
					result.setErrormessage("删除部门完毕，忽略删除的部门（有下级的部门）：" + hassonOrgs.toString());
				else
					result.setErrormessage("删除部门完毕!");
			}
			tm.commit();
			if(hasManagers){
				Event event = new EventImpl("",
						ACLEventType.USER_ROLE_INFO_CHANGE);
				EventHandle.sendEvent(event);
			}
			return result;
		} catch (Throwable e) {
			log.error("delete Batch orgIds failed:", e);
			result.setErrormessage(StringUtil.formatBRException(e));
			return result;
		}
		finally
		{
			tm.release();
		}


	}




}
