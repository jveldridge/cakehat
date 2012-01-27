package cakehat;

import cakehat.config.ConfigurationInfo;
import cakehat.config.ConfigurationInfoImpl;
import cakehat.services.CourseInfo;
import cakehat.services.CourseInfoImpl;
import cakehat.services.GradingServices;
import cakehat.database.DatabaseImpl;
import cakehat.database.DataServices;
import cakehat.database.Database;
import cakehat.export.CSVExporter;
import cakehat.export.Exporter;
import cakehat.printing.EnscriptPrintingService;
import cakehat.printing.LprPrintingService;
import cakehat.printing.PrintingService;
import cakehat.rubric.RubricManager;
import cakehat.rubric.RubricManagerImpl;
import cakehat.services.Constants;
import cakehat.services.ConstantsImpl;
import cakehat.database.DataServicesImpl;
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
     * Subclass this abstract class and implement allocate() so that it returns
     * an instance of whatever SingletonAllocation is parametrized on.
     * <br/><br/>
     * The reason for this class is to act as a "thunk" around what it is likely
     * constructed in the allocate() method. This is needed because the
     * constructor of an allocated object might make calls to the Allocator. If
     * this is the case, this could cause a problem. If for a test the object
     * being called for is going to be replaced by a test version, the test
     * version would not necessarily have been supplied yet. By providing all
     * allocations to be as SingletonAllocation's, when the first call to the
     * Allocator is made, the SingletonAllocation is used, thus meaning any
     * customized allocated objects are guaranteed to be used.
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

    private final SingletonAllocation<ConfigurationInfo> _configInfo;
    private final SingletonAllocation<CourseInfo> _courseInfo;
    private final SingletonAllocation<RubricManager> _rubricManager;
    private final SingletonAllocation<GradingServices> _gradingServices;
    private final SingletonAllocation<UserServices> _userServices;
    private final SingletonAllocation<DataServices> _dataServices;
    private final SingletonAllocation<FileSystemServices> _fileSystemServices;
    private final SingletonAllocation<PathServices> _pathServices;
    private final SingletonAllocation<StringManipulationServices> _stringManipServices;
    private final SingletonAllocation<Constants> _constants;
    private final SingletonAllocation<PrintingService> _landscapePrintingService;
    private final SingletonAllocation<PrintingService> _portraitPrintingService;
    private final SingletonAllocation<Exporter> _csvExporter;
    private final SingletonAllocation<Database> _database;
    private final SingletonAllocation<GeneralUtilities> _generalUtils;
    private final SingletonAllocation<ArchiveUtilities> _archiveUtils;
    private final SingletonAllocation<CalendarUtilities> _calendarUtils;
    private final SingletonAllocation<ExternalProcessesUtilities> _externalProcessesUtils;
    private final SingletonAllocation<FileSystemUtilities> _fileSystemUtils;
    private final SingletonAllocation<UserUtilities> _userUtils;
    private final SingletonAllocation<DataServicesV5> _dataServicesV5;

    /**
     * Creates the underlying instance of the Allocator. Any of the parameters
     * may be <code>null</code>. If the parameter is <code>null</code> then the
     * standard implementation will be used.
     *
     * @param configInfo
     * @param courseInfo
     * @param rubricManager
     * @param gradingServices
     * @param userServices
     * @param dataServices
     * @param pathServices
     * @param fileSystemServices
     * @param constants
     * @param database
     * @param landscapePrintingService
     * @param portraitPrintingService
     * @param csvExporter
     * @param generalUtils
     * @param archiveUtils
     * @param calendarUtils
     * @param externalProcessesUtils
     * @param fileSystemUtils
     * @param userUtils
     */
    private Allocator(SingletonAllocation<ConfigurationInfo> configInfo,
                      SingletonAllocation<CourseInfo> courseInfo,
                      SingletonAllocation<RubricManager> rubricManager,
                      SingletonAllocation<GradingServices> gradingServices,
                      SingletonAllocation<UserServices> userServices,
                      SingletonAllocation<DataServices> dataServices,
                      SingletonAllocation<FileSystemServices> fileSystemServices,
                      SingletonAllocation<PathServices> pathServices,
                      SingletonAllocation<StringManipulationServices> stringManipServices,
                      SingletonAllocation<Constants> constants,
                      SingletonAllocation<Database> database,
                      SingletonAllocation<PrintingService> landscapePrintingService,
                      SingletonAllocation<PrintingService> portraitPrintingService,
                      SingletonAllocation<Exporter> csvExporter,
                      SingletonAllocation<GeneralUtilities> generalUtils,
                      SingletonAllocation<ArchiveUtilities> archiveUtils,
                      SingletonAllocation<CalendarUtilities> calendarUtils,
                      SingletonAllocation<ExternalProcessesUtilities> externalProcessesUtils,
                      SingletonAllocation<FileSystemUtilities> fileSystemUtils,
                      SingletonAllocation<UserUtilities> userUtils,
                      SingletonAllocation<DataServicesV5> dataServicesV5)
    {
        if(configInfo == null)
        {
            _configInfo = new SingletonAllocation<ConfigurationInfo>()
                          { public ConfigurationInfo allocate() { return new ConfigurationInfoImpl(); } };
        }
        else
        {
            _configInfo = configInfo;
        }

        if(courseInfo == null)
        {
            _courseInfo = new SingletonAllocation<CourseInfo>()
                          { public CourseInfo allocate() { return new CourseInfoImpl(); } };
        }
        else
        {
            _courseInfo = courseInfo;
        }

        if(rubricManager == null)
        {
            _rubricManager = new SingletonAllocation<RubricManager>()
                             { public RubricManager allocate() { return new RubricManagerImpl(); } };
        }
        else
        {
            _rubricManager = rubricManager;
        }

        if(gradingServices == null)
        {
            _gradingServices = new SingletonAllocation<GradingServices>()
                               { public GradingServices allocate() { return new GradingServicesImpl(); } };
        }
        else
        {
            _gradingServices = gradingServices;
        }

        if(userServices == null)
        {
            _userServices = new SingletonAllocation<UserServices>()
                            { public UserServices allocate() { return new UserServicesImpl(); } };
        }
        else
        {
            _userServices = userServices;
        }

        if(dataServices == null)
        {
            _dataServices = new SingletonAllocation<DataServices>()
                            { public DataServices allocate() { return new DataServicesImpl(); } };
        }
        else
        {
            _dataServices = dataServices;
        }

        if(fileSystemServices == null)
        {
            _fileSystemServices = new SingletonAllocation<FileSystemServices>()
                            { public FileSystemServices allocate() { return new FileSystemServicesImpl(); } };
        }
        else
        {
            _fileSystemServices = fileSystemServices;
        }

        if(pathServices == null)
        {
            _pathServices = new SingletonAllocation<PathServices>()
                            { public PathServices allocate() { return new PathServicesImpl(); } };
        }
        else
        {
            _pathServices = pathServices;
        }

        if(stringManipServices == null)
        {
            _stringManipServices = new SingletonAllocation<StringManipulationServices>()
                            { public StringManipulationServices allocate() { return new StringManipulationServicesImpl(); } };
        }
        else
        {
            _stringManipServices = stringManipServices;
        }

        if(constants == null)
        {
            _constants = new SingletonAllocation<Constants>()
                        { public Constants allocate() { return new ConstantsImpl(); } };
        }
        else
        {
            _constants = constants;
        }

        if(database == null)
        {
            _database = new SingletonAllocation<Database>()
                            { public Database allocate() { return new DatabaseImpl(); } };
        }
        else
        {
            _database = database;
        }

        if(landscapePrintingService == null)
        {
            _landscapePrintingService = new SingletonAllocation<PrintingService>()
                                { public PrintingService allocate() { return new EnscriptPrintingService(); } };
        }
        else
        {
            _landscapePrintingService = landscapePrintingService;
        }

        if(portraitPrintingService == null)
        {
            _portraitPrintingService = new SingletonAllocation<PrintingService>()
                                { public PrintingService allocate() { return new LprPrintingService(); } };
        }
        else
        {
            _portraitPrintingService = portraitPrintingService;
        }

        if(csvExporter == null)
        {
            _csvExporter = new SingletonAllocation<Exporter>()
                           { public Exporter allocate() { return new CSVExporter(); } };
        }
        else
        {
            _csvExporter = csvExporter;
        }

        if(generalUtils == null)
        {
            _generalUtils = new SingletonAllocation<GeneralUtilities>()
                            { public GeneralUtilities allocate() { return new GeneralUtilitiesImpl(); } };
        }
        else
        {
            _generalUtils = generalUtils;
        }

        if(archiveUtils == null)
        {
            _archiveUtils = new SingletonAllocation<ArchiveUtilities>()
                            { public ArchiveUtilities allocate() { return new ArchiveUtilitiesImpl(); } };
        }
        else
        {
            _archiveUtils = archiveUtils;
        }

        if(calendarUtils == null)
        {
            _calendarUtils = new SingletonAllocation<CalendarUtilities>()
                             { public CalendarUtilities allocate() { return new CalendarUtilitiesImpl(); } };
        }
        else
        {
            _calendarUtils = calendarUtils;
        }

        if(externalProcessesUtils == null)
        {
            _externalProcessesUtils = new SingletonAllocation<ExternalProcessesUtilities>()
                                      { public ExternalProcessesUtilities allocate() { return new ExternalProcessesUtilitiesImpl(); } };
        }
        else
        {
            _externalProcessesUtils = externalProcessesUtils;
        }

        if(fileSystemUtils == null)
        {
            _fileSystemUtils = new SingletonAllocation<FileSystemUtilities>()
                               { public FileSystemUtilities allocate() { return new FileSystemUtilitiesImpl(); } };
        }
        else
        {
            _fileSystemUtils = fileSystemUtils;
        }

        if(userUtils == null)
        {
            _userUtils = new SingletonAllocation<UserUtilities>()
                         { public UserUtilities allocate() { return new UserUtilitiesImpl(); } };
        }
        else
        {
            _userUtils = userUtils;
        }
        if(dataServicesV5 == null)
        {
            _dataServicesV5 = new SingletonAllocation<DataServicesV5>()
                         { public DataServicesV5 allocate() { return new DataServicesV5Impl(); } };
        }
        else
        {
            _dataServicesV5 = dataServicesV5;
        }
    }

    public static ConfigurationInfo getConfigurationInfo()
    {
        return getInstance()._configInfo.getInstance();
    }

    public static CourseInfo getCourseInfo()
    {
        return getInstance()._courseInfo.getInstance();
    }

    public static RubricManager getRubricManager()
    {
        return getInstance()._rubricManager.getInstance();
    }

    public static GradingServices getGradingServices()
    {
        return getInstance()._gradingServices.getInstance();
    }

    public static UserServices getUserServices()
    {
        return getInstance()._userServices.getInstance();
    }

    public static DataServices getDataServices()
    {
        return getInstance()._dataServices.getInstance();
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

    public static Database getDatabase()
    {
        return getInstance()._database.getInstance();
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

    public static Exporter getCSVExporter()
    {
        return getInstance()._csvExporter.getInstance();
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

    /**
     * Outside of the Allocator class, this class should <b>ONLY<b/> used for
     * testing purposes.
     *
     * Constructs an Allocator with custom allocations. Any allocations that are
     * not provided will use the standard ones.
     */
    public static class Customizer
    {
        private SingletonAllocation<ConfigurationInfo> _configInfo;
        private SingletonAllocation<CourseInfo> _courseInfo;
        private SingletonAllocation<RubricManager> _rubricManager;
        private SingletonAllocation<GradingServices> _gradingServices;
        private SingletonAllocation<UserServices> _userServices;
        private SingletonAllocation<DataServices> _dataServices;
        private SingletonAllocation<FileSystemServices> _fileSystemServices;
        private SingletonAllocation<PathServices> _pathServices;
        private SingletonAllocation<StringManipulationServices> _stringManipServices;
        private SingletonAllocation<Constants> _constants;
        private SingletonAllocation<Database> _database;
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

        public Customizer setConfigurationInfo(SingletonAllocation<ConfigurationInfo> configInfo)
        {
            _configInfo = configInfo;
            return this;
        }

        public Customizer setCourseInfo(SingletonAllocation<CourseInfo> courseInfo)
        {
            _courseInfo = courseInfo;

            return this;
        }

        public Customizer setRubricManager(SingletonAllocation<RubricManager> rubricManager)
        {
            _rubricManager = rubricManager;

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

        public Customizer setDataServices(SingletonAllocation<DataServices> dataServices)
        {
            _dataServices = dataServices;

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

        public Customizer setDatabase(SingletonAllocation<Database> database)
        {
            _database = database;

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
            Allocator.INSTANCE = new Allocator(_configInfo,
                    _courseInfo,
                    _rubricManager,
                    _gradingServices,
                    _userServices,
                    _dataServices,
                    _fileSystemServices,
                    _pathServices,
                    _stringManipServices,
                    _constants,
                    _database,
                    _landscapePrintingService,
                    _portraitPrintingService,
                    _csvExporter,
                    _generalUtils,
                    _archiveUtils,
                    _calendarUtils,
                    _externalProcessesUtils,
                    _fileSystemUtils,
                    _userUtils,
                    _dataServicesV5);
        }
    }
}