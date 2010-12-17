package gradesystem.config;

import java.util.Collection;
import java.util.HashMap;
import java.util.Vector;

/**
 * Specifies the RUN, DEMO, and TESTER properties of a CodeHandin subclass.
 *
 * @author jak2
 */
public class LanguageSpecification
{
    //LANGUAGE name, e.g. "Java"
    private String _name = "";
    //RUN, DEMO, & TSETER modes
    private HashMap<String, Mode> _runModes = new HashMap<String, Mode>(),
                                  _demoModes = new HashMap<String, Mode>(),
                                  _testerModes = new HashMap<String, Mode>();

    /**
     * Used only by CodeHandin when trying to get a CodeHandin's
     * LanguageSpecification. (Necessary for reflection.) Don't use it otherwise.
     */
    LanguageSpecification() {}

    /**
     * Specifies how a CodeHandin subclass has set up its RUN, DEMO, and TESTER
     * modes.
     *
     * @param name The name specified by the LANGUAGE tag. For instance "Java".
     * @param runModes Run modes, may be null
     * @param demoModes Demo modes, may be null
     * @param testerModes Tester mode, may be null
     */
    LanguageSpecification(String name, Mode[] runModes, Mode[] demoModes, Mode[] testerModes)
    {
        _name = name;

        if(runModes != null)
        {
            for(Mode mode : runModes)
            {
                _runModes.put(mode.getName(), mode);
            }
        }

        if(demoModes != null)
        {
            for(Mode mode : demoModes)
            {
                _demoModes.put(mode.getName(), mode);
            }
        }

        if(testerModes != null)
        {
            for(Mode mode : testerModes)
            {
                _testerModes.put(mode.getName(), mode);
            }
        }

        //Register this LanguageSpecification
        ConfigurationOptions.registerLanguageSpecification(this);
    }

    @Override
    public String toString()
    {
        return _name;
    }

    /**
     * Name of the language this specifies.
     *
     * @return
     */
    public String getName()
    {
        return _name;
    }

    /**
     * If there is a run mode with the specified name.
     *
     * @param name
     * @return
     */
    public boolean hasRunMode(String name)
    {
        return _runModes.containsKey(name);
    }

    /**
     * If there is a demo mode with the specified name.
     *
     * @param name
     * @return
     */
    public boolean hasDemoMode(String name)
    {
        return _demoModes.containsKey(name);
    }

    /**
     * If there is a tester mode with the specified name.
     *
     * @param name
     * @return
     */
    public boolean hasTesterMode(String name)
    {
        return _testerModes.containsKey(name);
    }

    /**
     * Gets the run mode with this name. null is returned if there is no run
     * mode of this name.
     *
     * @param name
     * @return
     */
    public Mode getRunMode(String name)
    {
        return _runModes.get(name);
    }

    /**
     * Gets the demo mode with this name. null is returned if there is no run
     * mode of this name.
     *
     * @param name
     * @return
     */
    public Mode getDemoMode(String name)
    {
        return _demoModes.get(name);
    }

    /**
     * Gets the tester mode with this name. null is returned if there is no run
     * mode of this name.
     *
     * @param name
     * @return
     */
    public Mode getTesterMode(String name)
    {
        return _testerModes.get(name);
    }

    /**
     * Gets all of the run modes.
     *
     * @return
     */
    public Collection<Mode> getRunModes()
    {
        return _runModes.values();
    }

    /**
     * Gets all of the demo modes.
     *
     * @return
     */
    public Collection<Mode> getDemoModes()
    {
        return _demoModes.values();
    }

    /**
     * Gets all of the tester modes.
     * 
     * @return
     */
    public Collection<Mode> getTesterModes()
    {
        return _testerModes.values();
    }


    /**
     * A Property as defined in the configuration.
     */
    public static class Property
    {
        private String _name = "";
        private boolean _required = false;

        Property(String name, boolean required)
        {
            _name = name;
            _required = required;
        }

        /**
         * Name of the property.
         *
         * @return
         */
        public String getName()
        {
            return _name;
        }

        /**
         * If this property is required for the Mode it is part of.
         *
         * @return
         */
        public boolean isRequired()
        {
            return _required;
        }

        @Override
        public String toString()
        {
            return _name;
        }
    }


    /**
     * Representation of a RUN, DEMO, or TESTER mode.
     */
    public static class Mode
    {
        private String _name = "";
        private Collection<Property> _allProperties = new Vector<Property>(),
                                     _requiredProperties = new Vector<Property>();

        Mode(String name, Property... properties)
        {
            _name = name;

            for(Property property : properties)
            {
                _allProperties.add(property);

                if(property.isRequired())
                {
                    _requiredProperties.add(property);
                }
            }
        }

        /**
         * The properties name.
         *
         * @return
         */
        public String getName()
        {
            return _name;
        }

        @Override
        public String toString()
        {
            return _name;
        }

        /**
         * All properties required by this mode.
         *
         * @return
         */
        public Collection<Property> getRequiredProperties()
        {
            return _requiredProperties;
        }

        /**
         * All properties this mode has.
         * 
         * @return
         */
        public Collection<Property> getAllProperties()
        {
            return _allProperties;
        }
    }
}