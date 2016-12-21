package com.xdja.collectdata;

import java.util.ArrayList;
import java.util.Scanner;

class WatchThread extends Thread   {   
        Process   p;   
        boolean   over;  
        ArrayList<String> stream;  
        public WatchThread(Process p) {   
            this.p = p;   
            over = false;  
            stream = new ArrayList<String>();  
        }   
        public void run() {   
          try {   
               if(p == null)return;   
               Scanner br = new Scanner(p.getInputStream());  
               while (true) {   
                   if (p==null || over) break;   
                   while(br.hasNextLine()){  
                       String tempStream = br.nextLine();  
                       if(tempStream.trim()==null||tempStream.trim().equals(""))continue;  
                       stream.add(tempStream);  
                   }  
               }   
               } catch(Exception   e){e.printStackTrace();}   
        }  
          
        public void setOver(boolean   over)   {   
              this.over   =   over;   
        }  
        public ArrayList<String> getStream() {  
            return stream;  
        }  
    } 