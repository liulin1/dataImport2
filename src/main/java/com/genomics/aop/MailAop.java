package com.genomics.aop;

import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.bgi.parrot2.dao.SubProjectProcInfoDAO;
import org.bgi.parrot2.model.MailObject;
import org.bgi.parrot2.model.ReferenceDB;
import org.bgi.parrot2.model.SubProjectBackup;
import org.bgi.parrot2.model.SubProjectProcInfo;
import org.bgi.parrot2.service.ParrotMailService;
import org.bgi.parrot2.utils.Constants;
import org.bgi.parrot2.utils.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.regex.Pattern;

/**
 * Created by zhouliangfeng on 16/3/9.
 */
@Component
@Aspect
public class MailAop {

    private static final Logger logger = LoggerFactory.getLogger(MailAop.class);
    @Autowired
    private SubProjectProcInfoDAO subProjectProcInfoDAO;
    @Autowired
    private ParrotMailService parrotMailService;

    @After("execution(* org.springframework.data.repository.CrudRepository.save(*)) && args (list)")
    public void interceptProject(Iterable list)
            throws Throwable {
//        System.out.println("catching ...");
        list.forEach(this::doInterception);
    }

    private void doInterception(Object entity){
        String subProjectCode = null;
        State state = null;
        String projectOperator = null;
        String mailSubject = null;
        HashMap<String, String> map = new HashMap<>();

        if (entity instanceof SubProjectBackup) {
            SubProjectBackup item = (SubProjectBackup) entity;
            subProjectCode = item.getSubProject().getSubProjectId();
            state = item.getProcStatus();
            map.put("level", Constants.MAIL_LEVEL_PROJECT);
            map.put("status", state.toString());
            map.put("subproject", subProjectCode+" backup");
            mailSubject=egeDomain + " Notice: 项目备份状态有更新";
            projectOperator = subProjectProcInfoDAO.findValidUser(item.getSubProject().getId());
        } else if (entity instanceof SubProjectProcInfo) {
            SubProjectProcInfo item = (SubProjectProcInfo) entity;
            subProjectCode = item.getSubProject().getSubProjectId();
            state = item.getProcStatus();
            map.put("level", "project");
            map.put("status", state.toString());
            map.put("subproject", subProjectCode);
            mailSubject=egeDomain + " Notice: 项目状态有更新";
            projectOperator = subProjectProcInfoDAO.findValidUser(item.getSubProject().getId());
        } else if (entity instanceof ReferenceDB) {
            ReferenceDB item = (ReferenceDB) entity;
            subProjectCode = item.getDbName();
            state = item.getProcStatus();
            projectOperator = item.getCreateMan();
            mailSubject=egeDomain + " Notice: 参考数据库状态有更新";
        } else {
            return;
        }


        if (subProjectCode!=null)
            logger.info("subproject code: {}, status: {}", subProjectCode, state);

        String[] mailto = fixedMailto(projectOperator);

        MailObject mailObject = MailObject.builder()
                .catalog(catalog)
                .subject(mailSubject)
                .data(map)
                .from(from)
                .to(mailto)
                .build();
        parrotMailService.send(mailObject);
    }

    private String[] fixedMailto(String mail){
        if (!EMAIL_PATTERN.matcher(mail).matches()) {
            return adminMails;
        } else {
            return new String[]{mail};
        }

    }

    @Value("${mailservice.catalog}")
    private String catalog;

    @Value("${mailservice.admin-mails}")
    private String[] adminMails;

    @Value("${mailservice.mail.from:noreply@genomics.cn}")
    private String from;

    @Value("${mailservice.ege-domain:EGE}")
    private String egeDomain;

    public static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
                    + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$");

}
