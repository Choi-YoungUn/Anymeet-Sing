package com.ssafy.anymeetsong.kurento;

import java.io.*;
import java.lang.reflect.Field;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class RunShellScript {
	
//    private static class StreamGobbler implements Runnable {
//        private InputStream inputStream;
//        private Consumer<String> consumer;
//
//        public StreamGobbler(InputStream inputStream, Consumer<String> consumer) {
//            this.inputStream = inputStream;
//            this.consumer = consumer;
//        }
//
//        public void run() {
//            try {
//                new BufferedReader(new InputStreamReader(inputStream, "euc-kr")).lines()
//                        .forEach(consumer);
//            } catch (UnsupportedEncodingException e) {
//                e.printStackTrace();
//            }
//        }
//    }
    
    private final ConcurrentMap<String, Process> workingTasks = new ConcurrentHashMap<>();
    
    public void shella(int port_a, int port_v, String videoId, String roomId) throws IOException, InterruptedException {

        ProcessBuilder builder = new ProcessBuilder();
        System.out.println("Audio Port and Video Port " + Integer.toString(port_a) + " " + Integer.toString(port_v) + " " + videoId);
        builder.command("bash", "stream.sh", Integer.toString(port_a), Integer.toString(port_v), videoId);
        
        builder.directory(new File("/home/ubuntu"));

        Process process = builder.start();
        
//        try {
//			System.out.println(getUnixPID(process));
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
        workingTasks.put(roomId, process);
//        StreamGobbler streamGobbler =
//                new StreamGobbler(process.getInputStream(), System.out::println);
//        Executors.newSingleThreadExecutor().submit(streamGobbler);
//        int exitCode = process.waitFor();
//        assert exitCode == 0;
    }
    
    public void kill(String roomId) throws Exception {
    	System.out.println("Kill: Current Room id: " + roomId);
    	System.out.println("Kill: Map length: " + workingTasks.size());
    	Process process = workingTasks.get(roomId);
    	if(process != null)
    		System.out.println(process.isAlive());
    	else 
    		System.out.println("Process is empty");
    	
//		int exitCode = process.waitFor();
//		if(exitCode == 0) System.out.println("Terminate normally");
//		else System.out.println("Terminate abnormally");
		
    	killUnixProcess(process);
//    	process.destroy();
//    	
//    	process.getInputStream().close();
//    	process.getOutputStream().close();
//    	process.getErrorStream().close();
    	workingTasks.remove(roomId);
    	
    }
    
    public static int getUnixPID(Process process) throws Exception
    {
        System.out.println(process.getClass().getName());
        if (process.getClass().getName().equals("java.lang.UNIXProcess"))
        {
            Class cl = process.getClass();
            Field field = cl.getDeclaredField("pid");
            field.setAccessible(true);
            Object pidObject = field.get(process);
            return (Integer) pidObject;
        } else
        {
            throw new IllegalArgumentException("Needs to be a UNIXProcess");
        }
    }

    public static int killUnixProcess(Process process) throws Exception
    {
        int pid = getUnixPID(process);
        System.out.println("PID : " + pid);
        
        ProcessBuilder builder = new ProcessBuilder("bash", "-c", "kill $(pgrep -P " + pid + ")");

        System.out.println("kill $(pgrep -P " + pid + ")");
        
        builder.directory(new File("/home/ubuntu"));
        
        System.out.println(builder);
        
        return builder.start().waitFor();
    }
   	
}



