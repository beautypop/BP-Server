package common.schedule;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;

import models.Post;
import models.User;
import models.ViewSocialRelation;
import common.collection.Pair;
import common.utils.NanoSecondStopWatch;
import controllers.ElasticSearchController;
import email.SendgridEmailClient;

/**
 * Created by IntelliJ IDEA.
 * Date: 24/10/14
 * Time: 11:50 PM
 * To change this template use File | Settings | File Templates.
 */
public class CommandChecker {
    private static play.api.Logger logger = play.api.Logger.apply(CommandChecker.class);

    public static void checkCommandFiles() {
        File f = new File("command.txt");
        if (f.exists()) {
            try {
                DataInputStream in = new DataInputStream(new FileInputStream(f));
                BufferedReader br = new BufferedReader(new InputStreamReader(in));

                String strLine;
                while ((strLine = br.readLine()) != null)   {
                    performCommand(strLine);
                }

                in.close();
                // rename to prevent infinite loop.
                f.renameTo(new File("command_done.txt"));
            } catch (Exception e) {
                logger.underlyingLogger().error("Error in performCommand", e);
            }
        }
    }

    /**
     * 
     * 1) rebuildElasticIndexes
     * 2) exportUserEmails [email]
     * 3) exportDailyStats [daysBefore]
     */
    private static void performCommand(String commandLine) {
        if (commandLine.endsWith("DONE")) {
            return;
        }

        String[] params = commandLine.split("\\s");

        NanoSecondStopWatch sw = new NanoSecondStopWatch();
        
        logger.underlyingLogger().info("[command="+commandLine+"] performCommand() starts..");
        
        // rebuildElasticIndexes
        if (commandLine.startsWith("rebuildElasticIndexes")) {
            rebuildElasticIndexes(params);
        }
        // exportUserEmails
        else if (commandLine.startsWith("exportUserEmails")) {
            exportUserEmails(params);
        }
        // exportDailyStats
        else if (commandLine.startsWith("exportDailyStats")) {
            exportDailyStats(params);
        }
        
        
        //
        // TEMP TEMP TEMP !!!
        //
        
        
        // populateUserNumViews
        else if (commandLine.startsWith("populateUserNumViews")) {
            logger.underlyingLogger().info("populateUserNumViews()");
            for(User user : User.getEligibleUsersForFeed()){
                user.numViews = ViewSocialRelation.getUserViewsCount(user.id);
            }
        }
        
        

        /* 
        // importProducts
        else if (commandLine.startsWith("importProducts")) {
            if (tokens.length > 1) {
                String filePath = tokens[1];
                logger.underlyingLogger().info("Running bootstrapCommunityPosts with: "+filePath);
                ThreadLocalOverride.disableNotification(true);
                DataBootstrap.importProducts(filePath);
                ThreadLocalOverride.disableNotification(false);
            } else {
                logger.underlyingLogger().error("bootstrapCommunityPosts missing file path");
            }
        }
        
        // Check and assign comms for all users
        else if (commandLine.startsWith("assignCommunitiesToUsers")) {
        	if (tokens.length > 1) {
        		try {
	        		Long fromUserId = Long.parseLong(tokens[1]);
	        		Long toUserId = Long.parseLong(tokens[2]);
	        		logger.underlyingLogger().info("Running assignCommunitiesToUsers with: "+fromUserId+" "+toUserId);
	        		//CommunityTargetingEngine.assignSystemCommunitiesToUsers(fromUserId, toUserId);
	        		logger.underlyingLogger().info("Completed assignCommunitiesToUsers with: "+fromUserId+" "+toUserId);
        		} catch (NumberFormatException e) {
        			logger.underlyingLogger().error(
        					"assignCommunitiesToUsers with wrong param format - "+tokens[1]+" "+tokens[2], e);
        		} catch (Exception e) {
        			logger.underlyingLogger().error(
        					"assignCommunitiesToUsers with exception - "+e.getLocalizedMessage(), e);
        		}
        	} else {
                logger.underlyingLogger().error("assignCommunitiesToUsers missing fromUserId, toUserId");
            }
        }
        */
        
        sw.stop();
        logger.underlyingLogger().info("[command="+commandLine+"] performCommand() completed. Took "+sw.getElapsedMS()+"ms");
    }
    
    private static void rebuildElasticIndexes(String[] params) {
        logger.underlyingLogger().info("rebuildElasticIndexes() clean indexes");
        ElasticSearchController.cleanIndex();
        ElasticSearchController.refresh();
        
        logger.underlyingLogger().info("rebuildElasticIndexes() index posts");
        for(Post post : Post.getEligiblePostsForFeeds()){
            ElasticSearchController.addPostElasticSearch(post);
        }
        
        logger.underlyingLogger().info("rebuildElasticIndexes() index users");
        for(User user : User.getEligibleUsersForFeed()){
            ElasticSearchController.addUserElasticSearch(user);
        }
    }
    
    private static void exportUserEmails(String[] params) {
        if (params.length > 1) {
            String email = params[1];
            Pair<Integer,String> csv = User.exportUserEmails();
            logger.underlyingLogger().info("exportUserEmail() Count="+csv.first);

            SendgridEmailClient.getInstance().sendMail(email, "Target Android EDM users", csv.second);
            logger.underlyingLogger().info("exportUserEmails() Sent to "+email);
        } else {
            logger.underlyingLogger().error("exportUserEmails() missing email parameter");
        }
    }
    
    private static void exportDailyStats(String[] params) {
        if (params.length > 1) {
            Integer daysBefore = Integer.valueOf(params[1]);
            //CommunityStatistics.populatePastStats(daysBefore);
        } else {
            logger.underlyingLogger().error("exportDailyStats missing daysBefore parameter");
        }
    }
}
