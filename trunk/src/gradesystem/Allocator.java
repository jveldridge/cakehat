package gradesystem;

import gradesystem.config.ConfigurationInfo;
import gradesystem.config.ConfigurationInfoImpl;
import gradesystem.services.CourseInfo;
import gradesystem.services.CourseInfoImpl;
import gradesystem.services.GradingServices;
import gradesystem.database.DBWrapper;
import gradesystem.database.DatabaseIO;
import gradesystem.export.CSVExporter;
import gradesystem.export.Exporter;
import gradesystem.printing.EnscriptPrinter;
import gradesystem.printing.LprPrinter;
import gradesystem.printing.Printer;
import gradesystem.rubric.RubricManager;
import gradesystem.rubric.RubricManagerImpl;
import gradesystem.services.Constants;
import gradesystem.services.ConstantsImpl;
import gradesystem.services.FileSystemServices;
import gradesystem.services.FileSystemServicesImpl;
import gradesystem.services.GradingServicesImpl;
import gradesystem.services.StringManipulationServices;
import gradesystem.services.StringManipulationServicesImpl;
import gradesystem.services.PathServices;
import gradesystem.services.PathServicesImpl;
import gradesystem.services.UserServices;
import gradesystem.services.UserServicesImpl;
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
    private final SingletonAllocation<FileSystemServices> _fileSystemServices;
    private final SingletonAllocation<PathServices> _pathServices;
    private final SingletonAllocation<StringManipulationServices> _stringManipServices;
    private final SingletonAllocation<Constants> _constants;
    private final SingletonAllocation<DatabaseIO> _database;
    private final SingletonAllocation<Printer> _landscapePrinter;
    private final SingletonAllocation<Printer> _portraitPrinter;
    private final SingletonAllocation<Exporter> _csvExporter;
    private final SingletonAllocation<GeneralUtilities> _generalUtils;
    private final SingletonAllocation<ArchiveUtilities> _archiveUtils;
    private final SingletonAllocation<CalendarUtilities> _calendarUtils;
    private final SingletonAllocation<ExternalProcessesUtilities> _externalProcessesUtils;
    private final SingletonAllocation<FileSystemUtilities> _fileSystemUtils;
    private final SingletonAllocation<UserUtilities> _userUtils;

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
     * @param pathServices
     * @param fileSystemServices
     * @param constants
     * @param database
     * @param landscapePrinter
     * @param portraitPrinter
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
                      SingletonAllocation<FileSystemServices> fileSystemServices,
                      SingletonAllocation<PathServices> pathServices,
                      SingletonAllocation<StringManipulationServices> stringManipServices,
                      SingletonAllocation<Constants> constants,
                      SingletonAllocation<DatabaseIO> database,
                      SingletonAllocation<Printer> landscapePrinter,
                      SingletonAllocation<Printer> portraitPrinter,
                      SingletonAllocation<Exporter> csvExporter,
                      SingletonAllocation<GeneralUtilities> generalUtils,
                      SingletonAllocation<ArchiveUtilities> archiveUtils,
                      SingletonAllocation<CalendarUtilities> calendarUtils,
                      SingletonAllocation<ExternalProcessesUtilities> externalProcessesUtils,
                      SingletonAllocation<FileSystemUtilities> fileSystemUtils,
                      SingletonAllocation<UserUtilities> userUtils)
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
            _database = new SingletonAllocation<DatabaseIO>()
                            { public DatabaseIO allocate() { return new DBWrapper(); } };
        }
        else
        {
            _database = database;
        }

        if(landscapePrinter == null)
        {
            _landscapePrinter = new SingletonAllocation<Printer>()
                                { public Printer allocate() { return new EnscriptPrinter(); } };
        }
        else
        {
            _landscapePrinter = landscapePrinter;
        }

        if(portraitPrinter == null)
        {
            _portraitPrinter = new SingletonAllocation<Printer>()
                                { public Printer allocate() { return new LprPrinter(); } };
        }
        else
        {
            _portraitPrinter = portraitPrinter;
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

    public static DatabaseIO getDatabaseIO()
    {
        return getInstance()._database.getInstance();
    }

    public static Printer getLandscapePrinter()
    {
        return getInstance()._landscapePrinter.getInstance();
    }

    public static Printer getPortraitPrinter()
    {
        return getInstance()._portraitPrinter.getInstance();
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
        private SingletonAllocation<FileSystemServices> _fileSystemServices;
        private SingletonAllocation<PathServices> _pathServices;
        private SingletonAllocation<StringManipulationServices> _stringManipServices;
        private SingletonAllocation<Constants> _constants;
        private SingletonAllocation<DatabaseIO> _database;
        private SingletonAllocation<Printer> _landscapePrinter;
        private SingletonAllocation<Printer> _portraitPrinter;
        private SingletonAllocation<Exporter> _csvExporter;
        private SingletonAllocation<GeneralUtilities> _generalUtils;
        private SingletonAllocation<ArchiveUtilities> _archiveUtils;
        private SingletonAllocation<CalendarUtilities> _calendarUtils;
        private SingletonAllocation<ExternalProcessesUtilities> _externalProcessesUtils;
        private SingletonAllocation<FileSystemUtilities> _fileSystemUtils;
        private SingletonAllocation<UserUtilities> _userUtils;

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

        public Customizer setDatabase(SingletonAllocation<DatabaseIO> database)
        {
            _database = database;

            return this;
        }

        public Customizer setLandscapePrinter(SingletonAllocation<Printer> landscapePrinter)
        {
            _landscapePrinter = landscapePrinter;

            return this;
        }

        public Customizer setPortraitPrinter(SingletonAllocation<Printer> portraitPrinter)
        {
            _portraitPrinter = portraitPrinter;

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
                    _fileSystemServices,
                    _pathServices,
                    _stringManipServices,
                    _constants,
                    _database,
                    _landscapePrinter,
                    _portraitPrinter,
                    _csvExporter,
                    _generalUtils,
                    _archiveUtils,
                    _calendarUtils,
                    _externalProcessesUtils,
                    _fileSystemUtils,
                    _userUtils);
        }
    }
}
