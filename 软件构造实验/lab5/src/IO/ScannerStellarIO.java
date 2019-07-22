package IO;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import applications.ConcreteCircularOrbit;
import centralObject.Stellar;
import checkTxt.checkStellarSystemTxt;
import logs.Mylog;
import myExceptions.ParameterNumberException;
import myExceptions.SyntaxException;
import myExceptions.WrongAngleException;
import myExceptions.sameTagException;
import otherDirectory.label;
import otherDirectory.number;
import physicalObject.Planet;
import physicalObject.PlanetWithSatellite;
import track.PlanetTrack;

public class ScannerStellarIO implements StellarIO {
	public static void main(String[] args) throws IOException {
		ScannerStellarIO test=new ScannerStellarIO();
		ConcreteCircularOrbit system = test.constract("src/TXT/BigStellarSystem.txt");
		test.output(system);
	}

	@Override
	public ConcreteCircularOrbit constract(String filePath) throws IOException {
		Scanner in  = new Scanner(System.in);
		Scanner fileIn = new Scanner(new BufferedReader(new FileReader(filePath)));
	    Map<number,PlanetTrack> tracks = new HashMap<number,PlanetTrack>();
	    try {
	      checkStellarSystemTxt.check(filePath);
	    } catch (SyntaxException e) {
	      System.out.println("please choose another file path");
	      String file = in.nextLine();
	      return constract(file);
	    } catch (ParameterNumberException e) {
	      System.out.println("please choose another file path");
	      String file = in.nextLine();
	      return constract(file);
	    } catch (sameTagException e) {
	      System.out.println("please choose another file path");
	      String file = in.nextLine();
	      return constract(file);
	    } catch (WrongAngleException e) {
	      System.out.println("please choose another file path");
	      String file = in.nextLine();
	      return constract(file);
	    }
	    catch (IOException e) {
	      System.out.println("please choose another file path");
	      String file = in.nextLine();
	      return constract(file);
	    }
	    Mylog.logger.info("[Stellar] constract a StellarSystem from "+filePath);
	    List<PlanetTrack> list = new ArrayList<PlanetTrack>();
	    Stellar newSte = null;
	    File file = new File(filePath);
	    FileReader fr = new FileReader(file);
	    String temp = null;
	    do {
	      temp = fileIn.nextLine();
	    } while (temp   ==  null ||  temp.equals(""));
	    String regex = "Stellar ::= <([A-Za-z ]+),([A-Za-z0-9. ]+),([A-Za-z0-9. ]+)>";
	    Pattern pattern = Pattern.compile(regex); 
	    Matcher matcher = pattern.matcher(temp);
	    if (matcher.find()) {
	      newSte = new Stellar(new label(matcher.group(1).trim()),
	    new number(matcher.group(2).trim()),new number(matcher.group(3).trim()));
	    } else {
	      System.out.println("not match");
	    }


	    while (temp != null) {
	      if(fileIn.hasNextLine())
	        temp = fileIn.nextLine();
	      else
	        temp = null;
	      //System.out.println(temp);
	      if (temp == null) {
	        break;
	      }
	      String planetregex = "Planet ::= <([A-Za-z0-9 ]+),([A-Za-z ]+),([A-Za-z ]+),"
	          + "([e0-9. ]+),([e0-9. ]+),([e0-9. ]+),([CW ]+),([0-9. ]+)>";
	      pattern = Pattern.compile(planetregex);
	      matcher = pattern.matcher(temp);
	      if (matcher.find()) {
	        Planet p1 = new Planet(new label(matcher.group(1).trim()),
	            new label(matcher.group(2).trim()),new label(matcher.group(3).trim()),
	            new number(matcher.group(4).trim()),new number(matcher.group(5).trim()),
	            new number(matcher.group(6).trim()),
	            matcher.group(7).trim(),Double.parseDouble(matcher.group(8).trim()));
	        
	        number radius = new number(matcher.group(5).trim());
	        if(!tracks.containsKey(radius)) {
	      	  PlanetTrack t1 = new PlanetTrack(radius);
	            t1.ObjectOnTrack.add(new PlanetWithSatellite(p1));
	            tracks.put(radius, t1);
	        }
	        else {
	      	  PlanetTrack t1 = tracks.get(radius);
	      	  t1.ObjectOnTrack.add(new PlanetWithSatellite(p1));
	      	  tracks.put(radius, t1);
	        }
	      }
	    }
	    fileIn.close();
	    
	    for(Map.Entry<number, PlanetTrack> e:tracks.entrySet()) {
	  	  list.add(e.getValue());
	    }
	    
	    Collections.sort(list, new Comparator<PlanetTrack>() {

	      @Override
	  public int compare(PlanetTrack o1, PlanetTrack o2) {
	        BigDecimal r1 = o1.r.value;
	        BigDecimal r2 = o2.r.value;
	        if (r1.compareTo(r2) == 1) {
	          return 1;
	        }
	        if (r1.compareTo(r2) == -1) {
	          return -1;
	        }
	        return 0;
	      }
	    });
	    	    
	    ConcreteCircularOrbit target = new ConcreteCircularOrbit(list,newSte);
	    return target;
	}

	@Override
	public void output(ConcreteCircularOrbit system) {
		try {
		   	 Stellar stellar=system.getStellar();
			 File file = new File("src/TXT/StellarOutput.txt");

			 if (!file.exists()) {
			   file.createNewFile();
			 }

			 FileWriter fw = new FileWriter(file.getAbsoluteFile());
			 BufferedWriter bw = new BufferedWriter(fw);
			 String central = "Stellar ::= <" + stellar.getName().toString() + ","+
			 stellar.getRadius().toString() + ","+stellar.getWeight().toString() + ">\n";
			 bw.write(central);
			   
			 for (PlanetTrack t:system.getTrack()) {
				 for (PlanetWithSatellite p:t.ObjectOnTrack) {
					 Planet planet=p.planet;
					 String name = planet.getName().toString();
					 String state = planet.getState().toString();
					 String color = planet.getColor().toString();
					 String planetradius=planet.getPlanetRadius().toString();
					 String orbitalRadius = planet.getOrbitalRadius().toString();
					 String direction = planet.getDirection();
					 String speed = planet.getSpeed().toString();
					 String content="Planet ::= <" + name + "," + state + 
					 "," + color + "," + planetradius + "," + orbitalRadius + "," + speed + "," + 
					 direction + "," + planet.getAngle() + ">\n";
					 //System.out.println(content);
					 bw.write(content);
				 }
			 }
			   
			bw.close();
			} catch (IOException e) {
			  e.printStackTrace();
			}
		
	}

}
