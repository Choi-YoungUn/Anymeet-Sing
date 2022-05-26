package com.ssafy.anymeetsong.model.service;

import java.util.Random;

import javax.mail.Message.RecipientType;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.ssafy.anymeetsong.model.mapper.EmailMapper;

 
@Service
public class EmailServiceImpl implements EmailService{
	public final int VERIFICATION = 1, PASSWORD = 2;
 
	@Autowired
	private SqlSession sqlSession;
	
    @Autowired
    JavaMailSender emailSender;
    
    public String ePw;
 
    private MimeMessage createMessage(String to, int mode)throws Exception{
//        System.out.println("보내는 대상 : "+ to);
//        System.out.println("인증 번호 : "+ePw);
        MimeMessage  message = emailSender.createMimeMessage();
 
        message.addRecipients(RecipientType.TO, to);//보내는 대상
        String msgg="";
        
        if(mode == VERIFICATION) {
	        message.setSubject("AnyMeet&Song 회원가입 이메일 인증");//제목
	        
	        msgg+= "<div style='margin:100px;'>";
	        msgg+= "<h1> AnyMeetSing 회원가입 인증 메일입니다. </h1>";
	        msgg+= "<br>";
	        msgg+= "<p>아래 코드를 회원가입 창으로 돌아가 입력해주세요<p>";
	        msgg+= "<br>";
	        msgg+= "<div align='center' style='border:1px solid black; font-family:verdana';>";
	        msgg+= "<h3 style='color:blue;'>회원가입 인증 코드입니다.</h3>";
	        msgg+= "<div style='font-size:130%'>";
	        msgg+= "CODE : <strong>";
	        msgg+= ePw+"</strong><div><br/> ";
	        msgg+= "</div>";
        } else if(mode == PASSWORD) {
        	message.setSubject("AnyMeet&Song 임시 비밀번호 발급");//제목
	        
	        msgg+= "<div style='margin:100px;'>";
	        msgg+= "<h1> AnyMeetSing 임시 비밀번호 발송 메일입니다. </h1>";
	        msgg+= "<br>";
	        msgg+= "<p>아래 코드는 발급된 임시 비밀번호 입니다.<p>";
	        msgg+= "<br>";
	        msgg+= "<div align='center' style='border:1px solid black; font-family:verdana';>";
	        msgg+= "<h3 style='color:blue;'>임시 비밀번호입니다. 발급된 임시번호로 로그인하여 비밀번호를 바꿔주세요.</h3>";
	        msgg+= "<div style='font-size:130%'>";
	        msgg+= "CODE : <strong>";
	        msgg+= ePw+"</strong><div><br/> ";
	        msgg+= "</div>";
        }
        message.setText(msgg, "utf-8", "html");//내용
        message.setFrom(new InternetAddress("verify.prj@gmail.com","anymeetsing"));//보내는 사람
 
        return message;
    }
 
    public static String createKey() {
        StringBuffer key = new StringBuffer();
        Random rnd = new Random();
 
        for (int i = 0; i < 8; i++) { // 인증코드 8자리
            int index = rnd.nextInt(3); // 0~2 까지 랜덤
 
            switch (index) {
                case 0:
                    key.append((char) ((int) (rnd.nextInt(26)) + 97));
                    //  a~z  (ex. 1+97=98 => (char)98 = 'b')
                    break;
                case 1:
                    key.append((char) ((int) (rnd.nextInt(26)) + 65));
                    //  A~Z
                    break;
                case 2:
                    key.append((rnd.nextInt(10)));
                    // 0~9
                    break;
            }
        }
 
        return key.toString();
    }
    
    @Override
    public String sendSimpleMessage(String to, int mode) throws Exception {
    	ePw = createKey();
        MimeMessage message = createMessage(to, mode);
        try{	//예외처리
            emailSender.send(message);
        }catch(MailException es){
            es.printStackTrace();
            throw new IllegalArgumentException();
        }
        return ePw;
    }

	@Override
	public Boolean saveIdAndCodeToDB(String id, String code) throws Exception {
		// select로 이미 DB에 존재하는지 확인
		if(sqlSession.getMapper(EmailMapper.class).findId(id) != null) {
			sqlSession.getMapper(EmailMapper.class).deleteIdAndCode(id);
		}
		// id와 code 추가
		return sqlSession.getMapper(EmailMapper.class).insertIdAndCode(id, code) == 1;
	}

	@Override
	public int verifyCode(String id, String code) throws Exception {
		String target = sqlSession.getMapper(EmailMapper.class).findCodeById(id);
		
		if(target == null) return 401;	// id에 해당하는 code가 없으면 false
		if(!target.equals(code)) return 400;	// code가 일치하지 않으면 false
		
		int timediff = sqlSession.getMapper(EmailMapper.class).getSecondTillCreated(id);
		// insert한 시간과 현재 시간의 차이 (단위: 초). 3분 넘으면 false
		if(timediff > 180) return 402;
		
		// 모두 성공이라면, DB에서 정보 삭제해주고 성공코드 보내기
		sqlSession.getMapper(EmailMapper.class).deleteIdAndCode(id);
		return 200;
	}
 
}
