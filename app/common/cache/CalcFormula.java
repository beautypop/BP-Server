package common.cache;

import java.math.BigDecimal;
import java.util.Random;

import models.Post;

import org.joda.time.DateTime;
import org.joda.time.Days;

import play.Play;
import play.db.jpa.Transactional;
import common.thread.ThreadLocalOverride;
import common.utils.NanoSecondStopWatch;

/**
 * http://stackoverflow.com/questions/11653545/hot-content-algorithm-score-with-time-decay
 * 
 * @author keithlei
 */
public class CalcFormula {
	private static play.api.Logger logger = play.api.Logger.apply(CalcFormula.class);
	
	public static final int CALC_FORMULA_POINTS_VIEWS = Play.application().configuration().getInt("calc.formula.points.views");
	public static final int CALC_FORMULA_POINTS_LIKES = Play.application().configuration().getInt("calc.formula.points.likes");
	public static final int CALC_FORMULA_POINTS_CONVERSATIONS = Play.application().configuration().getInt("calc.formula.points.conversations");
	public static final int CALC_FORMULA_POINTS_BUYS = Play.application().configuration().getInt("calc.formula.points.buys");
	
	public static final Long FEED_SCORE_HIGH_BASE = Play.application().configuration().getLong("feed.score.high.base");
	public static final int FEED_SCORE_COMPUTE_BASE = Play.application().configuration().getInt("feed.score.compute.base");
	public static final Double FEED_SCORE_COMPUTE_MIN = Play.application().configuration().getDouble("feed.score.compute.min");
	public static final int FEED_SCORE_COMPUTE_DECAY_START = Play.application().configuration().getInt("feed.score.compute.decay.start");
	public static final int FEED_SCORE_COMPUTE_DECAY_VELOCITY = Play.application().configuration().getInt("feed.score.compute.decay.velocity");
	public static final int FEED_SCORE_RANDOMIZE_PERCENT = Play.application().configuration().getInt("feed.score.randomize.percent");
	
	private Random random = new Random();
	
	@Transactional
	public Long computeBaseScore(Post post) {
	    NanoSecondStopWatch sw = new NanoSecondStopWatch();
        logger.underlyingLogger().debug("computeBaseScore for p="+post.id);
        if (!ThreadLocalOverride.isServerStartingUp()) {
            logger.underlyingLogger().debug("   numComments="+post.numComments);
            logger.underlyingLogger().debug("   numViews="+post.numViews);
            logger.underlyingLogger().debug("   numLikes="+post.numLikes);
            logger.underlyingLogger().debug("   numConversations="+post.numConversations);
            logger.underlyingLogger().debug("   numBuys="+post.numBuys);
            logger.underlyingLogger().debug("   baseScoreAdjust="+post.baseScoreAdjust);
        }
        
        post.baseScore = (long) (
                post.numComments 
                + CALC_FORMULA_POINTS_VIEWS * post.numViews 
                + CALC_FORMULA_POINTS_LIKES * post.numLikes 
                + CALC_FORMULA_POINTS_CONVERSATIONS * post.numConversations 
                + CALC_FORMULA_POINTS_BUYS * post.numBuys 
                + FEED_SCORE_COMPUTE_BASE);
        
        post.save();
        
        sw.stop();
        logger.underlyingLogger().debug("computeBaseScore completed with baseScore="+post.baseScore+". Took "+sw.getElapsedSecs()+"s");
        
        return post.baseScore;
	}
	
	public Double computeTimeScore(Post post) {
	    NanoSecondStopWatch sw = new NanoSecondStopWatch();
	    logger.underlyingLogger().debug("---");
        logger.underlyingLogger().debug("computeTimeScore for p="+post.id+" date="+post.getCreatedDate()+" baseScore="+post.baseScore);
        
        Double timeScore = (double) Math.max(post.baseScore, 1);
        Double timeDiff = Math.abs(Days.daysBetween(new DateTime(post.getCreatedDate()), new DateTime()).getDays()) / 7D;
        timeDiff = (double) Math.ceil(timeDiff);    // timeDiff in terms of weeks
        if (timeDiff > FEED_SCORE_COMPUTE_DECAY_START) {
            timeDiff -= FEED_SCORE_COMPUTE_DECAY_START;
            Double discountFactor = getDiscountFactor(timeDiff);
            timeScore = timeScore * discountFactor;
            logger.underlyingLogger().debug("timeDecay timeDiff="+timeDiff+" => discountFactor="+discountFactor);
        } else {
            logger.underlyingLogger().debug("timeDecay timeDiff="+timeDiff+" => NO discountFactor");
        }
        
        // make sure at least min score before adjust
        timeScore = (double) Math.max(timeScore, FEED_SCORE_COMPUTE_MIN);
        
        // adjust score
        if (post.baseScoreAdjust != null) {
            timeScore += post.baseScoreAdjust;     // can be negative
        }
        
        BigDecimal bd = new BigDecimal(timeScore);
        bd = bd.setScale(5, BigDecimal.ROUND_HALF_UP);
        timeScore = bd.doubleValue();
        
        post.timeScore = timeScore;
        post.save();
        
        sw.stop();
        logger.underlyingLogger().debug("computeTimeScore completed with timeScore="+timeScore+". Took "+sw.getElapsedSecs()+"s");
        
        return timeScore;
	}
	
	public Double randomizeScore(Post post) {
	    Double timeScore = post.timeScore;
	    if (timeScore == null || timeScore == 0D) {
	        timeScore = computeTimeScore(post);
	    }
        int min = 100 - FEED_SCORE_RANDOMIZE_PERCENT;
        int max = 100 + FEED_SCORE_RANDOMIZE_PERCENT;
        Double percent = (random.nextInt(max - min) + min) / 100D;
        Double randomizedScore = timeScore * percent;
        //logger.underlyingLogger().debug("randomizeScore completed with timeScore="+timeScore+" %="+percent+" randomizedScore="+randomizedScore);
        return randomizedScore;
    }
	
	private Double getDiscountFactor(Double timeDiff) {
	    return Math.exp(-FEED_SCORE_COMPUTE_DECAY_VELOCITY * timeDiff);
	}
}
