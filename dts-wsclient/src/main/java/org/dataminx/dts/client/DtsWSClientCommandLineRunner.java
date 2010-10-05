/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.dataminx.dts.client;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;
import java.util.StringTokenizer;
import org.dataminx.dts.ws.client.DataTransferServiceClient;
import org.dataminx.dts.ws.client.DataTransferServiceClientImpl;
import org.dataminx.schemas.dts.x2009.x07.messages.SubmitJobRequestDocument;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 *
 * @author mtd28985
 */
public class DtsWSClientCommandLineRunner {

    /** Default Spring config file location */
    public static final String[] DEFAULT_SPRING_CLASSPATH = {
        "/org/dataminx/dts/client/client-context.xml"};
    private DataTransferServiceClient mClient;

    public DtsWSClientCommandLineRunner() {
        final String[] classpath = getSpringClasspath();
        final ClassPathXmlApplicationContext context =
                new ClassPathXmlApplicationContext(classpath, DtsWSClientCommandLineRunner.class);
        context.start();
        mClient = (DataTransferServiceClientImpl) context.getBean("dataTransferServiceClient");
    }

    public void run(String commandName, String input) throws Exception {
        if (commandName.equals("submit")) {
            if (input == null) {
                System.out.println("sunmit command needs a submit doc !");
            } else {
                final File f = new File(input);
                final SubmitJobRequestDocument dtsJob = SubmitJobRequestDocument.Factory.parse(f);
                final String jobResourceKey = mClient.submitJob(dtsJob);
                System.out.println("A user job has been submitted to the DTS successfully. Its JobResourceKey is: " + jobResourceKey);
                try {
                    BufferedWriter out = new BufferedWriter(new FileWriter("test.txt"));
                    out.write(jobResourceKey);
                    out.close();
                } catch (IOException e) {
                    System.out.println("Exception: " +e.getMessage());
                }
            }
        } else if (commandName.equals("cancel")) {
            mClient.cancelJob(input);
        } else if (commandName.equals("resume")) {
            mClient.resumeJob(input);
        } else if (commandName.equals("getjobstatus")) {
            mClient.getJobStatus(input);
        } else {
            System.out.println("Command name is not recognised for: " + commandName);
        }
    }

    private String[] getSpringClasspath() {
        return DEFAULT_SPRING_CLASSPATH;
    }

    public static void main(String args[]) throws Exception {
        System.out.println("starting DTSWSClient...");
        DtsWSClientCommandLineRunner commandRunner = new DtsWSClientCommandLineRunner();
        System.out.println("A DTSWSClient is ready to use.");

        String msg;

        Scanner scan = new Scanner(System.in);

        System.out.println("Please enter a command (or 'bye'):");

        do {
            msg = scan.nextLine();
            if (msg.equalsIgnoreCase("bye")) {
                break;
            }
            StringTokenizer st1 = new StringTokenizer(msg);
            if ((st1.countTokens() == 0 ) ||(st1.countTokens() > 2)) {
                System.out.println("Command format is not recognised for: " + msg);
            } else {
                String cmd = st1.nextToken();
                String para = st1.nextToken();
                System.out.println("Run: " + cmd + " " + para);

                try {
                    commandRunner.run(cmd, para);
                } catch (Exception e) {
                    System.out.println("Run: " + cmd + " failed: " + e.getMessage());
                }
            }
        } while (true);
    }
}
