package Grotznak.bcVote;


import java.util.Hashtable;
import java.util.List;

import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.entity.Player;



public class Votings {
// Object[] myt = {};
		public String name;
		public List<Player> all;
		public List<Player> yes;
		public List<Player> no;
 
	
	public Votings( String name) {
	//this.myt. = "test";
		this.name = name;
		this.all = null;
		this.yes = null;
		this.no =null; 		 
	}

    public boolean dovote(World world, Player voter, boolean vote, Hashtable<String,String> CONFIG,Hashtable<String,String> LANG,String name) {
       this.name = name;    	
       Server s = voter.getServer();
      
       this.all = s.matchPlayer("");
       
       
	   if (null==this.yes) {
		   this.yes = s.matchPlayer("");
		   this.yes.clear();	   		   
	   }
	   
	   if (null==this.no) {
   		   this.no = s.matchPlayer("");
		   this.no.clear();	   		   
	   }
	   
	   clrNonPermission(world, s);
		  
	   Double req = Double.valueOf( CONFIG.get("required-yes-percentage") )/100;
	   Double min = Double.valueOf( CONFIG.get("minimum-agree-percentage") )/100;
	   Double allcount = (double) this.all.size();
	   
	   if (vote){		  
			  if(!this.yes.contains(voter)){
				this.yes.add(voter);
				//  voter.sendMessage("is put in");
			  }
			  if(this.no.contains(voter)){
				  this.no.remove(voter);
			  }

			  
			  sync(world, s);  
			  
			  Double yescount = (double) this.yes.size();
			  Double nocount = (double) this.no.size();
	
		      Double allvotes = yescount + nocount;
		      
		      String head = LANG.get("SUM_HEAD");
		      head = head.replaceAll("%yes%",yescount.toString());
		      head = head.replaceAll("%no%",nocount.toString());
		      head = head.replaceAll("%all%",allcount.toString());
		      
		      String body = LANG.get("SUM_BODY");
		      body = body.replaceAll("%votes%", String.valueOf(Math.round((yescount / allcount )*100)));
		      body = body.replaceAll("%yespercentage%", String.valueOf(Math.round((yescount/allvotes)*100)));

		      String foot = LANG.get("SUM_FOOT");
		      foot = foot.replaceAll("%req%", String.valueOf(Math.round(req*100)));
		      foot = foot.replaceAll("%min%", String.valueOf(Math.round(min*100)));
		      
		      voter.sendMessage( head);
			  voter.sendMessage( body);
			  voter.sendMessage( foot);
			 
				   
			 if (((yescount / allcount ) > req ) &&  ((yescount/allvotes)>=min)){
				 //voter.sendMessage("send true");
				 return true;
				 
			 } else {
				 //voter.sendMessage("send false");
				 return false;		
			 }
	  } else {
		  
			  if(this.yes.contains(voter)){
				  this.yes.remove(voter);
			  }
			  if(!no.contains(voter)){
				  no.add(voter);
			  }
			  sync(world, s);  
		      return false;
	  } 
	  
	}
    
    public void sync (World world, Server s){
    	//this.all = s.matchPlayer("");
    	//s.broadcastMessage("Sync ALL Votes:");
    	List<Player> delthis = s.matchPlayer("");
    	delthis.clear();
    	
    	if (!this.yes.isEmpty()){
	    	for (Player item: this.yes) {
	    		//s.broadcastMessage("Sync YES Votes:"+ item.getDisplayName()); 
		    		if (!this.all.contains(item)){
		    		//	s.broadcastMessage("-:"+ item.getDisplayName()+ "not found, deleting");
		    			delthis.add(item);
		    		} 
	    		}
	    	for (Player item: delthis) {
	    		this.yes.remove(item);
	    	}
	    	delthis.clear();
    	}
    	if (!this.no.isEmpty()){
	    	for (Player item: this.no) {
	    		//s.broadcastMessage("Sync no Votes:"+ item.getDisplayName()); 
		    		if (!this.all.contains(item)){
		    		//	s.broadcastMessage("-:"+ item.getDisplayName()+ "not found, deleting");
		    			delthis.add(item);
		    		} else {
		    		//	s.broadcastMessage("-:"+ item.getDisplayName()+ "found, OK");
		    		}
	    		}
	    	for (Player item: delthis) {
	    		this.no.remove(item);
	    	}
	    	delthis.clear();
    	}
    	
    }
    
    public void clrNonPermission (World world, Server s){
    	List<Player> delthis = s.matchPlayer("");
    	delthis.clear();
    	String perString = "";
 
    	if (this.name == "Time") {
    		perString = "bcvote.time";
    	}
    	if (this.name == "Weather") {
    		perString = "bcvote.weather";
    	}

    	if (!this.yes.isEmpty()){
	    	for (Player item: this.yes) {
	    		    if (this.yes.contains(item)&&!(item.hasPermission(perString) || item.hasPermission("bcvote.*"))){ 
		    			delthis.add(item);
		    		}    
	    		}
	    	for (Player item: delthis) {
	    		this.yes.remove(item);
	    	}
	    	delthis.clear();
    	}
    	if (!this.no.isEmpty()){
	    	for (Player item: this.no) {	    		
	    		if (this.no.contains(item)&&!(item.hasPermission(perString) || item.hasPermission("bcvote.*"))){ 
		    			delthis.add(item);
		    		} 
	    		}
	    	for (Player item: delthis) {
	    		this.no.remove(item);
	    	}
	    	delthis.clear();
    	}
    	if (!this.all.isEmpty()){
	    	for (Player item: this.all) {	    		
	    		if (this.all.contains(item)&&!(item.hasPermission(perString) || item.hasPermission("bcvote.*"))){ 
		    			delthis.add(item);
		    		} 
	    		}
	    	for (Player item: delthis) {
	    		this.all.remove(item);
	    	}
	    	delthis.clear();
    	}
    }

}

