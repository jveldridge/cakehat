package cakehat;

import cakehat.email.EmailManager;
import cakehat.email.EmailManagerImpl;
import cakehat.services.CourseInfo;
import cakehat.services.CourseInfoImpl;
import cakehat.services.GradingServices;
import cakehat.export.Exporter;
import cakehat.printing.EnscriptPrintingService;
import cakehat.printing.LprPrintingService;
import cakehat.printing.PrintingService;
import cakehat.services.Constants;
import cakehat.services.ConstantsImpl;
import cakehat.newdatabase.DatabaseV5;
import cakehat.newdatabase.DatabaseV5Impl;
import cakehat.newdatabase.DataServicesV5;
import cakehat.newdatabase.DataServicesV5Impl;
import cakehat.services.FileSystemServices;
import cakehat.services.FileSystemServicesImpl;
import cakehat.services.GradingServicesImpl;
import cakehat.services.StringManipulationServices;
import cakehat.services.StringManipulationServicesImpl;
import cakehat.services.PathServices;
import cakehat.services.PathServicesImpl;
import cakehat.services.UserServices;
import cakehat.services.UserServicesImpl;
import cakehat.views.shared.gradingsheet.GradingSheetManager;
import cakehat.views.shared.gradingsheet.GradingSheetManagerImpl;
import support.testutils.TestUtilities;
import support.utils.ArchiveUtilities;
import support.utils.ArchiveUtilitiesImpl;
import support.utils.CalendarUtilities;
import support.utils.CalendarUtilitiesImpl;
import support.utils.ExternalProcessesUtilities;
import support.utils.ExternalProcessesUtilitiesImpl;
import support.utils.FileSystemUtilities;
import support.utils.FileSystemUtilitiesImpl;
import support.utils.GeneralUtilities;
import support.utils.GeneralUtilitiesImpl;
import support.utils.UserUtilities;
import support.utils.UserUtilitiesImpl;

/**
 * Used to statically get references to utilities and services.
 *
 * @author jak2 (Joshua Kaplan)
 */
public class Allocator
{
    /**
     * The singleton instance used by the Allocator.
     */
    private static Allocator INSTANCE;

    /**
     * Retrieves the singleton instance, and if it does not exist, creates the
     * standard implementation.
     *
     * The only situation in which the standard implementation would not be used
     * is if a custom one was created by Allocator.Customizer for testing
     * purposes.
     * 
     * @return
     */
    private static Allocator getInstance()
    {
       if(INSTANCE == null)
       {
           //Sets INSTANCE to the standard implementaton
           new Customizer().createSingletonInstance();
       }

       return INSTANCE;
    }

    /**
     * Subclass this abstract class and implement {@link #allocate()} so that it returns an instance of whatever
     * SingletonAllocation is parameterized on.
     * <br/><br/>
     * The reason for this class is to act as a "thunk" around what it is likely constructed in the allocate() method.
     * This is needed because the constructor of an allocated object might make calls to the Allocator. If this is the
     * case, this could cause a problem. If for a test the object being called for is going to be replaced by a test
     * version, the test version would not necessarily have been supplied yet. By providing all allocations to be as
     * SingletonAllocation's, when the first call to the Allocator is made, the SingletonAllocation is used, thus
     * meaning any customized allocated objects are guaranteed to be used.
     * <br/><br/>
     * Essentially, this is an implementation of laziness.
     *
     * @param <T>
     */
    public static abstract class SingletonAllocation<T>
    {
        private T _instance = null;

        public T getInstance()
        {
            if(_instance == null)
            {
                _instance = this.allocate();
            }

            return _instance;
        }

        /**
         * This method should never be called by any other class. It should
         * only be called by this class, and implemented by subclasses.
         * @return
         */
        protected abstract T allocate();
    }

    private final SingletonAllocation<CourseInfo> _courseInfo;
    private final SingletonAllocation<GradingServices> _gradingServices;
    private final SingletonAllocation<UserServices> _userServices;
    private final SingletonAllocation<FileSystemServices> _fileSystemServices;
    private final SingletonAllocation<PathServices> _pathServices;
    private final SingletonAllocation<StringManipulationServices> _stringManipServices;
    private final SingletonAllocation<Constants> _constants;
    private final SingletonAllocation<PrintingService> _landscapePrintingService;
    private final SingletonAllocation<PrintingService> _portraitPrintingService;
    private final SingletonAllocation<DatabaseV5> _databaseV5;
    private final SingletonAllocation<GeneralUtilities> _generalUtils;
    private final SingletonAllocation<ArchiveUtilities> _archiveUtils;
    private final SingletonAllocation<CalendarUtilities> _calendarUtils;
    private final SingletonAllocation<ExternalProcessesUtilities> _externalProcessesUtils;
    private final SingletonAllocation<FileSystemUtilities> _fileSystemUtils;
    private final SingletonAllocation<UserUtilities> _userUtils;
    private final SingletonAllocation<DataServicesV5> _dataServicesV5;
    private final SingletonAllocation<GradingSheetManager> _gradingSheetManager;
    private final SingletonAllocation<EmailManager> _emailManager;

    /**
     * Creates the underlying instance of the Allocator. Any of the parameters may be {@code null}. If the parameter is
     * {@code null} then the standard implementation will be null.
     */
    private Allocator(Customizer customizer)
    {
        if(customizer._courseInfo == null)
        {
            _courseInfo = new SingletonAllocation<CourseInfo>()
                          { public CourseInfo allocate() { return new CourseInfoImpl(); } };
        }
        else
        {
            _courseInfo = customizer._courseInfo;
        }

        if(customizer._gradingServices == null)
        {
            _gradingServices = new SingletonAllocation<GradingServices>()
                               { public GradingServices allocate() { return new GradingServicesImpl(); } };
        }
        else
        {
            _gradingServices = customizer._gradingServices;
        }

        if(customizer._userServices == null)
        {
            _userServices = new SingletonAllocation<UserServices>()
                            { public UserServices allocate() { return new UserServicesImpl(); } };
        }
        else
        {
            _userServices = customizer._userServices;
        }

        if(customizer._fileSystemServices == null)
        {
            _fileSystemServices = new SingletonAllocation<FileSystemServices>()
                            { public FileSystemServices allocate() { return new FileSystemServicesImpl(); } };
        }
        else
        {
            _fileSystemServices = customizer._fileSystemServices;
        }

        if(customizer._pathServices == null)
        {
            _pathServices = new SingletonAllocation<PathServices>()
                            { public PathServices allocate() { return new PathServicesImpl(); } };
        }
        else
        {
            _pathServices = customizer._pathServices;
        }

        if(customizer._stringManipServices == null)
        {
            _stringManipServices = new SingletonAllocation<StringManipulationServices>()
                            { public StringManipulationServices allocate() { return new StringManipulationServicesImpl(); } };
        }
        else
        {
            _stringManipServices = customizer._stringManipServices;
        }

        if(customizer._constants == null)
        {
            _constants = new SingletonAllocation<Constants>()
                        { public Constants allocate() { return new ConstantsImpl(); } };
        }
        else
        {
            _constants = customizer._constants;
        }
        
        if(customizer._databaseV5 == null)
        {
            _databaseV5 = new SingletonAllocation<DatabaseV5>()
                            { public DatabaseV5 allocate() { return new DatabaseV5Impl(); } };
        }
        else
        {
            _databaseV5 = customizer._databaseV5;
        }

        if(customizer._landscapePrintingService == null)
        {
            _landscapePrintingService = new SingletonAllocation<PrintingService>()
                                { public PrintingService allocate() { return new EnscriptPrintingService(); } };
        }
        else
        {
            _landscapePrintingService = customizer._landscapePrintingService;
        }

        if(customizer._portraitPrintingService == null)
        {
            _portraitPrintingService = new SingletonAllocation<PrintingService>()
                                { public PrintingService allocate() { return new LprPrintingService(); } };
        }
        else
        {
            _portraitPrintingService = customizer._portraitPrintingService;
        }

        if(customizer._generalUtils == null)
        {
            _generalUtils = new SingletonAllocation<GeneralUtilities>()
                            { public GeneralUtilities allocate() { return new GeneralUtilitiesImpl(); } };
        }
        else
        {
            _generalUtils = customizer._generalUtils;
        }

        if(customizer._archiveUtils == null)
        {
            _archiveUtils = new SingletonAllocation<ArchiveUtilities>()
                            { public ArchiveUtilities allocate() { return new ArchiveUtilitiesImpl(); } };
        }
        else
        {
            _archiveUtils = customizer._archiveUtils;
        }

        if(customizer._calendarUtils == null)
        {
            _calendarUtils = new SingletonAllocation<CalendarUtilities>()
                             { public CalendarUtilities allocate() { return new CalendarUtilitiesImpl(); } };
        }
        else
        {
            _calendarUtils = customizer._calendarUtils;
        }

        if(customizer._externalProcessesUtils == null)
        {
            _externalProcessesUtils = new SingletonAllocation<ExternalProcessesUtilities>()
                                      { public ExternalProcessesUtilities allocate() { return new ExternalProcessesUtilitiesImpl(); } };
        }
        else
        {
            _externalProcessesUtils = customizer._externalProcessesUtils;
        }

        if(customizer._fileSystemUtils == null)
        {
            _fileSystemUtils = new SingletonAllocation<FileSystemUtilities>()
                               { public FileSystemUtilities allocate() { return new FileSystemUtilitiesImpl(); } };
        }
        else
        {
            _fileSystemUtils = customizer._fileSystemUtils;
        }

        if(customizer._userUtils == null)
        {
            _userUtils = new SingletonAllocation<UserUtilities>()
                         { public UserUtilities allocate() { return new UserUtilitiesImpl(); } };
        }
        else
        {
            _userUtils = customizer._userUtils;
        }
        
        if(customizer._dataServicesV5 == null)
        {
            _dataServicesV5 = new SingletonAllocation<DataServicesV5>()
                         { public DataServicesV5 allocate() { return new DataServicesV5Impl(); } };
        }
        else
        {
            _dataServicesV5 = customizer._dataServicesV5;
        }
        
        if(customizer._gradingSheetManager == null)
        {
            _gradingSheetManager = new SingletonAllocation<GradingSheetManager>()
                    { public GradingSheetManager allocate() { return new GradingSheetManagerImpl(); } };
        }
        else
        {
            _gradingSheetManager = customizer._gradingSheetManager;
        }
            
        if(customizer._emailManager == null)
        {
            _emailManager = new SingletonAllocation<EmailManager>()
                    { public EmailManager allocate() { return new EmailManagerImpl(); } };
        }
        else
        {
            _emailManager = customizer._emailManager;
        }
    }

    public static CourseInfo getCourseInfo()
    {
        return getInstance()._courseInfo.getInstance();
    }

    public static GradingServices getGradingServices()
    {
        return getInstance()._gradingServices.getInstance();
    }

    public static UserServices getUserServices()
    {
        return getInstance()._userServices.getInstance();
    }

    public static FileSystemServices getFileSystemServices()
    {
        return getInstance()._fileSystemServices.getInstance();
    }

    public static PathServices getPathServices()
    {
        return getInstance()._pathServices.getInstance();
    }

    public static StringManipulationServices getStringManipulationServices()
    {
        return getInstance()._stringManipServices.getInstance();
    }

    public static Constants getConstants()
    {
        return getInstance()._constants.getInstance();
    }
    
    public static DatabaseV5 getDatabaseV5()
    {
        return getInstance()._databaseV5.getInstance();
    }

    public static DataServicesV5 getDataServicesV5() 
    {
        return getInstance()._dataServicesV5.getInstance();
    }

    public static PrintingService getLandscapePrintingService()
    {
        return getInstance()._landscapePrintingService.getInstance();
    }

    public static PrintingService getPortraitPrintingService()
    {
        return getInstance()._portraitPrintingService.getInstance();
    }

    public static GeneralUtilities getGeneralUtilities()
    {
        return getInstance()._generalUtils.getInstance();
    }

    public static ArchiveUtilities getArchiveUtilities()
    {
        return getInstance()._archiveUtils.getInstance();
    }

    public static CalendarUtilities getCalendarUtilities()
    {
        return getInstance()._calendarUtils.getInstance();
    }

    public static ExternalProcessesUtilities getExternalProcessesUtilities()
    {
        return getInstance()._externalProcessesUtils.getInstance();
    }

    public static FileSystemUtilities getFileSystemUtilities()
    {
        return getInstance()._fileSystemUtils.getInstance();
    }

    public static UserUtilities getUserUtilities()
    {
        return getInstance()._userUtils.getInstance();
    }
    
    public static GradingSheetManager getGradingSheetManager()
    {
        return getInstance()._gradingSheetManager.getInstance();
    }
    
    public static EmailManager getEmailManager()
    {
        return getInstance()._emailManager.getInstance();
    }

    /**
     * Outside of the Allocator class, this class should <strong>ONLY</strong> used for testing purposes.
     * <br/><br/>
     * Constructs an Allocator with custom allocations. Any allocations that are not provided will use the standard
     * ones.
     */
    public static class Customizer
    {
        private SingletonAllocation<CourseInfo> _courseInfo;
        private SingletonAllocation<GradingServices> _gradingServices;
        private SingletonAllocation<UserServices> _userServices;
        private SingletonAllocation<FileSystemServices> _fileSystemServices;
        private SingletonAllocation<PathServices> _pathServices;
        private SingletonAllocation<StringManipulationServices> _stringManipServices;
        private SingletonAllocation<Constants> _constants;
        private SingletonAllocation<DatabaseV5> _databaseV5;
        private SingletonAllocation<PrintingService> _landscapePrintingService;
        private SingletonAllocation<PrintingService> _portraitPrintingService;
        private SingletonAllocation<Exporter> _csvExporter;
        private SingletonAllocation<GeneralUtilities> _generalUtils;
        private SingletonAllocation<ArchiveUtilities> _archiveUtils;
        private SingletonAllocation<CalendarUtilities> _calendarUtils;
        private SingletonAllocation<ExternalProcessesUtilities> _externalProcessesUtils;
        private SingletonAllocation<FileSystemUtilities> _fileSystemUtils;
        private SingletonAllocation<UserUtilities> _userUtils;
        private SingletonAllocation<DataServicesV5> _dataServicesV5;
        private SingletonAllocation<GradingSheetManager> _gradingSheetManager;
        private SingletonAllocation<EmailManager> _emailManager;

        public Customizer setCourseInfo(SingletonAllocation<CourseInfo> courseInfo)
        {
            _courseInfo = courseInfo;

            return this;
        }

        public Customizer setGradingServices(SingletonAllocation<GradingServices> gradingServices)
        {
            _gradingServices = gradingServices;

            return this;
        }

        public Customizer setUserServices(SingletonAllocation<UserServices> userServices)
        {
            _userServices = userServices;

            return this;
        }

        public Customizer setFileSystemServices(SingletonAllocation<FileSystemServices> fileSystemServices)
        {
            _fileSystemServices = fileSystemServices;

            return this;
        }

        public Customizer setPathServices(SingletonAllocation<PathServices> pathServices)
        {
            _pathServices = pathServices;

            return this;
        }

        public Customizer setLocalizationServices(SingletonAllocation<StringManipulationServices> stringManipServices)
        {
            _stringManipServices = stringManipServices;

            return this;
        }

        public Customizer setConstants(SingletonAllocation<Constants> constants)
        {
            _constants = constants;

            return this;
        }
        
        public Customizer setDatabaseV5(SingletonAllocation<DatabaseV5> databaseV5)
        {
            _databaseV5 = databaseV5;

            return this;
        }

        public Customizer setLandscapePrintingService(SingletonAllocation<PrintingService> landscapePrintingService)
        {
            _landscapePrintingService = landscapePrintingService;

            return this;
        }

        public Customizer setPortraitPrintingService(SingletonAllocation<PrintingService> portraitPrintingService)
        {
            _portraitPrintingService = portraitPrintingService;

            return this;
        }

        public Customizer setCsvExporter(SingletonAllocation<Exporter> csvExporter)
        {
            _csvExporter = csvExporter;

            return this;
        }

        public Customizer setGeneralUtils(SingletonAllocation<GeneralUtilities> generalUtils)
        {
            _generalUtils = generalUtils;

            return this;
        }

        public Customizer setArchiveUtils(SingletonAllocation<ArchiveUtilities> archiveUtils)
        {
            _archiveUtils = archiveUtils;

            return this;
        }

        public Customizer setCalendarUtils(SingletonAllocation<CalendarUtilities> calendarUtils)
        {
            _calendarUtils = calendarUtils;

            return this;
        }

        public Customizer setExternalProcessesUtils(SingletonAllocation<ExternalProcessesUtilities> externalProcessesUtils)
        {
            _externalProcessesUtils = externalProcessesUtils;

            return this;
        }

        public Customizer setFileSystemUtils(SingletonAllocation<FileSystemUtilities> fileSystemUtils)
        {
            _fileSystemUtils = fileSystemUtils;

            return this;
        }

        public Customizer setUserUtils(SingletonAllocation<UserUtilities> userUtils)
        {
            _userUtils = userUtils;

            return this;
        }
        
        public Customizer setDataServicesV5(SingletonAllocation<DataServicesV5> dataServicesV5)
        {
            _dataServicesV5 = dataServicesV5;
            
            return this;
        }
        
        public Customizer setEmailManager(SingletonAllocation<EmailManager> emailManager)
        {
            _emailManager = emailManager;
            
            return this;
        }

        /**
         * <b>WARNING: THIS METHOD IS FOR TESTING PURPOSES ONLY.</b>
         *
         * Sets the underlying singleton instance used by the Allocator.
         *
         * @param instance
         */
        public void customize()
        {
            TestUtilities.checkJUnitRunning();
            createSingletonInstance();
        }

        /**
         * Sets the Allocator's INSTANCE field which holds its singleton reference.
         */
        private void createSingletonInstance()
        {
            Allocator.INSTANCE = new Allocator(this);
        }
    }
}