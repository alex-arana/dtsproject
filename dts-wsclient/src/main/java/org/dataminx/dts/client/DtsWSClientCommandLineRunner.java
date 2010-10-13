/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.dataminx.dts.client;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Calendar;
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
    public static final String[] DEFAULT_SPRING_CLASSPATH = {"/org/dataminx/dts/client/client-context.xml"};
    public static final String DEFAULT_JOB_RESOURCE_KEY_FILE_NAME = "jobresourcekeyfile.default";
    public static final String JOB_RESOURCE_KEY_FILE_NAME = "jobresourcekeyfile";

    public DtsWSClientCommandLineRunner() {
    }

    private String[] getSpringClasspath() {
        return DEFAULT_SPRING_CLASSPATH;
    }
    
    private void printusage() {
        System.out.println("DTS Web Service Client Command Usage:\n");
        System.out.println("-submit [-url <value>] [-un <value>] [-pw <value>] [-trustStore <value>] [-trustStorePassword <value>] [-submitJobRequestFile <pathtofile>]\n\n" + "-cancel [-un <value>] [-pw <value>] [-trustStore <value>] [-trustStorePassword <value>] [-submissionFile=<pathtofile>] \n\n" + "-resume [-un <value>] [-pw <value>] [-trustStore <value>] [-trustStorePassword <value>] [-submissionFile=<pathtofile>]\n\n" + "-getstatus [-un <value>] [-pw <value>] [-trustStore <value>] [-trustStorePassword <value>] [-submissionFile=<pathtofile>]\n\n" + "-help\n");
    }
    public static void main(String args[]) throws Exception {

        DtsWSClientCommandLineRunner commandRunner = new DtsWSClientCommandLineRunner();

        StringBuffer input = new StringBuffer();
        if (args.length > 0) {
            input.append(args[0]);
            for (int i = 1; i < args.length; i++) {
                input.append(' ');
                input.append(args[i]);
            }
            try {
                commandRunner.run(input.toString());
            } catch (Exception e) {
                System.out.println("The command failed: " + e.getMessage());
            }
        } else {
            System.out.println("Please type in a command and its options...\n");
            commandRunner.printusage();
        }
    }

    private void run(String input) throws Exception {
        StringTokenizer tokens = new StringTokenizer(input);
        while (tokens.hasMoreTokens()) {
            String cmd = tokens.nextToken();
            if (cmd.equals("-submit")) {
                String url = null;
                String un = null;
                String pw = null;
                String trustStore = null;
                String trustStorePassword = null;
                String submitJobRequestFile = null;
                while (tokens.hasMoreTokens()) {
                    String option = tokens.nextToken();
                    if (option.equals("-url")) {
                        if (tokens.hasMoreTokens()) {
                            String nt = tokens.nextToken();
                            if (!nt.startsWith("-")) {
                                url = nt;
                            } else {
                                System.out.println("-url value is empty");
                                return;
                            }
                        } else {
                            System.out.println("-url value is empty");
                            return;
                        }
                    } else if (option.equals("-un")) {
                        if (tokens.hasMoreTokens()) {
                            String nt = tokens.nextToken();
                            if (!nt.startsWith("-")) {
                                un = nt;
                            } else {
                                System.out.println("-un value is empty");
                                return;
                            }
                        } else {
                            System.out.println("-un value is empty");
                            return;
                        }
                    } else if (option.equals("-pw")) {
                        if (tokens.hasMoreTokens()) {
                            String nt = tokens.nextToken();
                            if (!nt.startsWith("-")) {
                                pw = nt;
                            } else {
                                System.out.println("-pw value is empty");
                                return;
                            }
                        } else {
                            System.out.println("-pw value is empty");
                            return;
                        }
                    } else if (option.equals("-truststore")) {
                        if (tokens.hasMoreTokens()) {
                            String nt = tokens.nextToken();
                            if (!nt.startsWith("-")) {
                                trustStore = nt;
                            } else {
                                System.out.println("-trustStore value is empty");
                                return;
                            }
                        } else {
                            System.out.println("-truststore value is empty");
                            return;
                        }
                    } else if (option.equals("-trustStorePassword")) {
                        if (tokens.hasMoreTokens()) {
                            String nt = tokens.nextToken();
                            if (!nt.startsWith("-")) {
                                trustStorePassword = nt;
                            } else {
                                System.out.println("-trustStorePassword value is empty");
                                return;
                            }
                        } else {
                            System.out.println("-trustStorePassword value is empty");
                            return;
                        }
                    } else if (option.equals("-submitJobRequestFile")) {
                        if (tokens.hasMoreTokens()) {
                            String nt = tokens.nextToken();
                            if (!nt.startsWith("-")) {
                                submitJobRequestFile = nt;
                            } else {
                                System.out.println("-submitJobRequestFile value is empty");
                                return;
                            }
                        } else {
                            System.out.println("-submitJobRequestFile value is empty");
                            return;
                        }
                    } else {
                        System.out.println("option: " + option + " is not recognised for coammand: " + cmd);
                        return;
                    }
                }
                if (url == null) {
                    System.out.println("-url value is empty");
                    return;
                } else {
                    System.setProperty("dtsws.url", url);
                }
                if (un == null) {
                    System.out.println("-un value is empty");
                    return;
                } else {
                    System.setProperty("auth.username", un);
                }
                if (pw == null) {
                    System.out.println("-pw value is empty");
                    return;
                } else {
                    System.setProperty("auth.password", pw);
                }
                if (trustStore != null) {
                    System.setProperty("javax.net.ssl.trustStore", trustStore);
                }
                if (trustStorePassword != null) {
                    System.setProperty("javax.net.ssl.trustStorePassword", trustStorePassword);
                }
                if (submitJobRequestFile == null) {
                    System.out.println("-submitJobRequestFile value is empty");
                    return;
                }
                ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(getSpringClasspath(), DtsWSClientCommandLineRunner.class);
                context.start();
                DataTransferServiceClient mClient = (DataTransferServiceClientImpl) context.getBean("dataTransferServiceClient");
                final File f = new File(submitJobRequestFile);
                final SubmitJobRequestDocument dtsJob = SubmitJobRequestDocument.Factory.parse(f);
                final String jobResourceKey = mClient.submitJob(dtsJob);
                System.out.println("A user job has been submitted to the DTS successfully. Its JobResourceKey is: " + jobResourceKey);

                BufferedWriter out = null;
                File outFile = new File(DEFAULT_JOB_RESOURCE_KEY_FILE_NAME);
                if (!outFile.exists()) {
                    out = new BufferedWriter(new FileWriter(outFile));
                } else {
                    java.sql.Timestamp currentTimestamp = new java.sql.Timestamp(Calendar.getInstance().getTime().getTime());
                    String fileName = JOB_RESOURCE_KEY_FILE_NAME + currentTimestamp.getTime();
                    out = new BufferedWriter(new FileWriter(fileName));
                }
                out.write(url);
                out.newLine();
                out.write(jobResourceKey);
                out.close();
            } else if (cmd.equals("-cancel") || cmd.equals("-resume") || cmd.equals("-getstatus")) {
                String un = null;
                String pw = null;
                String trustStore = null;
                String trustStorePassword = null;
                String submissionFile = null;
                while (tokens.hasMoreTokens()) {
                    String option = tokens.nextToken();
                    if (option.equals("-un")) {
                        if (tokens.hasMoreTokens()) {
                            String nt = tokens.nextToken();
                            if (!nt.startsWith("-")) {
                                un = nt;
                            } else {
                                System.out.println("-un value is empty");
                                return;
                            }
                        } else {
                            System.out.println("-un value is empty");
                            return;
                        }
                    } else if (option.equals("-pw")) {
                        if (tokens.hasMoreTokens()) {
                            String nt = tokens.nextToken();
                            if (!nt.startsWith("-")) {
                                pw = nt;
                            } else {
                                System.out.println("-pw value is empty");
                                return;
                            }
                        } else {
                            System.out.println("-pw value is empty");
                            return;
                        }
                    } else if (option.equals("-truststore")) {
                        if (tokens.hasMoreTokens()) {
                            String nt = tokens.nextToken();
                            if (!nt.startsWith("-")) {
                                trustStore = nt;
                            } else {
                                System.out.println("-trustStore value is empty");
                                return;
                            }
                        } else {
                            System.out.println("-truststore value is empty");
                            return;
                        }
                    } else if (option.equals("-trustStorePassword")) {
                        if (tokens.hasMoreTokens()) {
                            String nt = tokens.nextToken();
                            if (!nt.startsWith("-")) {
                                trustStorePassword = nt;
                            } else {
                                System.out.println("-trustStorePassword value is empty");
                                return;
                            }
                        } else {
                            System.out.println("-trustStorePassword value is empty");
                            return;
                        }
                    } else if (option.equals("-submissionFile")) {
                        if (tokens.hasMoreTokens()) {
                            String nt = tokens.nextToken();
                            if (!nt.startsWith("-")) {
                                submissionFile = nt;
                            } else {
                                if (!nt.equals("-un")&&!nt.equals("-pw")&&!nt.equals("-truststore")&&!nt.equals("-trustStorePassword")&&!nt.equals("-submissionFile")){
                                    System.out.println("option: " + nt + " is not recognised for coammand: " + cmd);
                                    return;
                                }
                                if (!(new File(DEFAULT_JOB_RESOURCE_KEY_FILE_NAME).exists())) {
                                    System.out.println("-submissionFile value is empty and the file jobresourcekeyfile.default is missing");
                                    return;
                                } else{
                                    submissionFile = DEFAULT_JOB_RESOURCE_KEY_FILE_NAME;
                                }
                            }
                        } else {
                            if (!(new File(DEFAULT_JOB_RESOURCE_KEY_FILE_NAME).exists())) {
                                    System.out.println("-submissionFile value is empty and the file jobresourcekeyfile.default is missing");
                                    return;
                                } else{
                                    submissionFile = DEFAULT_JOB_RESOURCE_KEY_FILE_NAME;
                                }
                        }
                    } else {
                        System.out.println("option: " + option + " is not recognised for coammand: " + cmd);
                        return;
                    }
                }
                if (un == null) {
                    System.out.println("-un value is empty");
                    return;
                } else {
                    System.setProperty("auth.username", un);
                }
                if (pw == null) {
                    System.out.println("-pw value is empty");
                    return;
                } else {
                    System.setProperty("auth.password", pw);
                }
                if (trustStore != null) {
                    System.setProperty("javax.net.ssl.trustStore", trustStore);
                }
                if (trustStorePassword != null) {
                    System.setProperty("javax.net.ssl.trustStorePassword", trustStorePassword);
                }
                if (submissionFile == null) {
                    System.out.println("-submissionFile value is empty");
                    return;
                }
                String wsurl = null;
                String jobresourcekey = null;
                BufferedReader in = new BufferedReader(new FileReader(submissionFile));
                wsurl = in.readLine();
                jobresourcekey = in.readLine();
                in.close();
                System.setProperty("dtsws.url", wsurl);
                ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(getSpringClasspath(), DtsWSClientCommandLineRunner.class);
                context.start();
                DataTransferServiceClient mClient = (DataTransferServiceClientImpl) context.getBean("dataTransferServiceClient");
                if (cmd.equals("-cancel")) {
                    mClient.cancelJob(jobresourcekey);
                }
                if (cmd.equals("-resume")) {
                    mClient.resumeJob(jobresourcekey);
                }
                if (cmd.equals("-getstatus")) {
                    mClient.getJobStatus(jobresourcekey);
                }
            } else if (cmd.equals("-help")) {
                if (!tokens.hasMoreTokens()) {
                    printusage();
                } else {
                    System.out.println("No options available for the -help command.");
                }
                return;
            } else {
                System.out.println(cmd + " is not a recognised command");
                printusage();
                return;
            }
        }
    }
}
