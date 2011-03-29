package cakehat.views.backend.assignmentdist;

/**
 * Must be implemented by classes that call the RubricManager 
 * <code>distributeRubrics(...)</code> method.  The
 * <code>updatePercentDone(...)</code> method is called by the RubricManager
 * each time a new rubric has been distributed to keep the caller informed
 * of the status of the distribution.
 *
 * @author jeldridg
 */
public interface DistributionRequester {

    public void updatePercentDone(int newPercentDone);

    /**
     * Defuault implementation whose <code>updatePercentDone(...)</code> method
     * does nothing.  Callers using this implementation will not actually be
     * informed of the progress of the rubric distribution.
     */
    public static DistributionRequester DO_NOTHING_REQUESTER = new DistributionRequester() {

        public void updatePercentDone(int newPercentDone) {
            //do nothing
        }
    };
}

