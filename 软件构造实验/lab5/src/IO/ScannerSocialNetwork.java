package IO;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import applications.SocialNetworkCircle;
import centralObject.CentralUser;
import checkTxt.checkSocialNetworkCircleTxt;
import logs.Mylog;
import myExceptions.IntimacyOutOfRangeException;
import myExceptions.ParameterNumberException;
import myExceptions.SyntaxException;
import myExceptions.sameTagException;
import otherDirectory.label;
import physicalObject.Friend;
import relations.SocialTie;

public class ScannerSocialNetwork implements SocialNetworkIO {
	  private List<SocialTie> relations = new ArrayList<SocialTie>();
	  private List<Friend> friends = new ArrayList<Friend>();
	  private CentralUser user;
	  private Friend f1;
	  /*
	   * RI：
	   * relations：朋友关系
	   * friendnum：朋友数量
	   * friends：存储所有朋友
	   * user：centralUser类型，中心使用者
	   * tracks：朋友关系
	   * filepath:文件路径，要求为String类型
	   * 
	   * AF：
	   *  根据读入文件来构建社交网络
	   *  
	   * safety from rep exposure:
	   * All fields are private;
	   * friendNum是int型，filePath是String型，所以 是immutable类型的
	   */
	  
	  //IO使用方法
	  public static void main(String[] args) {
		  ScannerSocialNetwork test = new ScannerSocialNetwork();
		  long start = System.currentTimeMillis();
		  SocialNetworkCircle system = test.Constract("src/TXT/BigSocialNetworkCircle.txt");
		  long end = System.currentTimeMillis();
		  System.out.println("scanner建立社交轨道系统花费的时间是" + (end - start));
		  try {
			test.output(system);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
  }

		
  public void constract(String filePath) throws IOException {
	  Scanner fileIn = new Scanner(new BufferedReader(new FileReader(filePath)));
	  do {
		  String social = fileIn.nextLine();
		  String regex = "Friend ::= <(.*?),(.*?),(.*?)>";
          Pattern pattern = Pattern.compile(regex);
          Matcher matcher = pattern.matcher(social);
          if (matcher.find()) {
        	label name = new label(matcher.group(1).trim());
  	        int age = Integer.parseInt(matcher.group(2).trim());
  	        char sex = matcher.group(3).trim().charAt(0);
  	        Friend f = new Friend(name, age, 1000, sex);
  	        friends.add(f);
          } else {
            	regex = "SocialTie ::= <(.*?),(.*?),(.*?)>";
                pattern = Pattern.compile(regex); 
                matcher = pattern.matcher(social);
                if (matcher.find()) {
                	String name1 = matcher.group(1).trim();
        	        String name2 = matcher.group(2).trim();
        	        double intimacy = Double.parseDouble(matcher.group(3));
        	        Friend f1 = new Friend(new label(name1), 0, 0, 'M');
        	        Friend f2 = new Friend(new label(name2), 0, 0, 'M');
        	        f1.addFriend(f2);;
        	        f1.addIntimacy(intimacy);
        	        f2.addFriend(f1);;
        	        f2.addIntimacy(intimacy);
        	        SocialTie st = new SocialTie(f1, f2);
        	        st.setIntimacy(intimacy);
        	        relations.add(st);
                } else {
                	regex = "CentralUser ::= <([A-Za-z]+),([0-9]+),([MF])>";
                	pattern = Pattern.compile(regex); 
            	    matcher = pattern.matcher(social);
            	    if (matcher.matches()) {
            	      label name = new label(matcher.group(1));
            	      int age = Integer.parseInt(matcher.group(2));
            	      char sex = matcher.group(3).charAt(0);
            	      user = new CentralUser(name,age,sex);
            	      f1 = new Friend(name, age, 0, sex);
            	      if (!friends.contains(f1)) {
            	        friends.add(f1);
            	      }
            	    }
                }
            }
	  } while(fileIn.hasNextLine());
  }
	  

	  /**
	   * 根据外部文件构造社交轨道系统.
	   * @return 根据外部文件构造的社交轨道系统
	   */
      @Override
	  public SocialNetworkCircle Constract(String filePath) {
	    try {
	      if (checkSocialNetworkCircleTxt.check(filePath)) {
	        Mylog.logger.info("[SocialNetwork]" + "constract network from " + filePath);
	        constract(filePath);
	        SocialNetworkCircle s = new SocialNetworkCircle(friends,relations,user,f1);
	        return s;
	      }
	    } catch (IOException | SyntaxException | ParameterNumberException | sameTagException
	        | IntimacyOutOfRangeException e) {
	      Scanner in = new Scanner(System.in);
	      System.out.println("please input another file path ");
	      String file = in.nextLine();
	      return this.Constract(file);
	    }
	    return null;
	    
	  }
	  
	  @Override
	  public void output(SocialNetworkCircle system) throws IOException {
			
			File file = new File("src/TXT/SocialNetworkOutput.txt");
			
			CentralUser user = system.getUser();

			 if (!file.exists()) {
			   file.createNewFile();
			 }
			 
			 FileWriter fw = new FileWriter("src/TXT/SocialNetworkOutput.txt");
			 
			 String central = "CentralUser ::= <" + user.getName().toString() + ","
			     + user.getAge() + "," +user.getSex()+">\n";
			 fw.write(central);
			 
			 for (Friend f:system.getFriends()) {
				 String friend = "Friend ::= <" + f.getname().toString()
				     + "," + f.getAge() + "," + f.getSex() + ">\n";
				 if (!f.getname().equals(user.getName())) {
				   fw.write(friend);
				 }
				 
			 }
			 
			 for (SocialTie t:system.getRelations()) {
				 String relation = "SocialTie ::= <" + t.object1.getname().toString()
				     + "," + t.object2.getname().toString() + "," + t.getIntimacy()
				     + ">\n";
				 fw.write(relation);
			 }
			 
			 fw.close();
		}	  

	  
	}

