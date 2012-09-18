package cakehat.database;

import cakehat.database.DatabaseTestHelpers.EqualityAsserter;
import cakehat.Allocator;
import cakehat.Allocator.SingletonAllocation;
import com.google.common.collect.ImmutableSet;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Set;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import static org.junit.Assert.*;

/**
 * Tests for {@link Database} methods related to properties stored in the database.
 * 
 * @author jeldridg
 */
public class DatabasePropertiesTest {
    
    private Database _database;
    
    @Rule
    public ExpectedException _thrown = ExpectedException.none();
    
    public DatabasePropertiesTest() throws IOException {
        _database = new DatabaseImpl(Allocator.getFileSystemUtilities().createTempFile("tempDB", "db"));

        SingletonAllocation<Database> dbioAlloc =
            new SingletonAllocation<Database>() {
                @Override
                public Database allocate() { return _database; };
            };
        new Allocator.Customizer().setDatabase(dbioAlloc).customize();
    }
    
    @Before
    public void setUp() throws SQLException {
        _database.resetDatabase();
    }
    
    @Test
    public void testSetGetStringProperty() throws SQLException {
        String account = "cakehat@cs.brown.edu";
        _database.putPropertyValue(DbPropertyValue.DbPropertyKey.EMAIL_ACCOUNT,
                                   new DbPropertyValue<String>(account));
        
        DbPropertyValue<String> property = _database.getPropertyValue(DbPropertyValue.DbPropertyKey.EMAIL_ACCOUNT);
        assertEquals(account, property.getValue());
    }
    
    @Test
    public void testSetGetBooleanProperty() throws SQLException {
        boolean attach = true;
        _database.putPropertyValue(DbPropertyValue.DbPropertyKey.ATTACH_GRADING_SHEET,
                                   new DbPropertyValue<Boolean>(true));
        
        DbPropertyValue<Boolean> property = _database.getPropertyValue(DbPropertyValue.DbPropertyKey.ATTACH_GRADING_SHEET);
        assertEquals(attach, property.getValue());
    }
    
    @Test
    public void testGetNotifyAddressesBeforeAnyPut() throws SQLException {
        Set<DbNotifyAddress> addresses = _database.getNotifyAddresses();
        assertEquals(0, addresses.size());
    }
    
    @Test
    public void testPutNotifyAddressWithInvalidId() throws SQLException {
        String addressString = "cakehat@cs.brown.edu";
        int invalidId = -1;
        
        _thrown.expect(SQLException.class);
        _thrown.expectMessage(DatabaseTestHelpers.getInvalidIdErrMsg(invalidId));
        
        DbNotifyAddress addressIn = new DbNotifyAddress(invalidId, addressString);
        
        _database.putNotifyAddresses(ImmutableSet.of(addressIn));
    }
    
    @Test
    public void testPutNotifyAddressWithNullAddress() throws SQLException {
        _thrown.expect(SQLException.class);
        _thrown.expectMessage(DatabaseTestHelpers.getNonNullConstraintViolationErrMsg("notifyaddresses", "address"));
        
        String nullAddress = null;
        
        DbNotifyAddress addressIn = new DbNotifyAddress(nullAddress);
        _database.putNotifyAddresses(ImmutableSet.of(addressIn));
    }
    
    private final EqualityAsserter<DbNotifyAddress> DB_NOTIFY_ADDRESS_EQ_C = 
            new EqualityAsserter<DbNotifyAddress>() {
                @Override
                public void assertEqual(DbNotifyAddress t1, DbNotifyAddress t2) {
                    assertEquals(t1.getId(), t2.getId());
                    assertEquals(t1.getAddress(), t2.getAddress());
                }
            };
    
    @Test
    public void testPutGetSingleNotifyAddress() throws SQLException {
        String addressString = "cakehat@cs.brown.edu";
        DbNotifyAddress addressIn = new DbNotifyAddress(addressString);
        
        _database.putNotifyAddresses(ImmutableSet.of(addressIn));
        //database method must update the ID of the element it is given
        assertNotNull(addressIn.getId());
        
        Set<DbNotifyAddress> addresses = _database.getNotifyAddresses();
        assertEquals(1, addresses.size());
        
        DatabaseTestHelpers.assertSetContainsGivenElements(DB_NOTIFY_ADDRESS_EQ_C, addresses, addressIn);
    }
    
    @Test
    public void testPutGetPutGetSingleNotifyAddress() throws SQLException {
        String initialAddressString = "cakehat@cs.brown.edu";
        String subsequentAddressString = "another@email.com";
        
        DbNotifyAddress address = new DbNotifyAddress(initialAddressString);
        _database.putNotifyAddresses(ImmutableSet.of(address));
        Integer id = address.getId();
        assertNotNull(id);
        
        Set<DbNotifyAddress> addresses = _database.getNotifyAddresses();
        assertEquals(1, addresses.size());
        DatabaseTestHelpers.assertSetContainsGivenElements(DB_NOTIFY_ADDRESS_EQ_C, addresses, address);
        
        address.setAddress(subsequentAddressString);
        _database.putNotifyAddresses(ImmutableSet.of(address));
        //ID should not have changed
        assertEquals(id, address.getId());
        
        addresses = _database.getNotifyAddresses();
        assertEquals(1, addresses.size());
        DatabaseTestHelpers.assertSetContainsGivenElements(DB_NOTIFY_ADDRESS_EQ_C, addresses, address);
    }

    @Test
    public void testPutGetMultipleNotifyAddressesSimultaneously() throws SQLException {
        String address1String = "cakehat@cs.brown.edu";
        String address2String = "another@email.com";
        
        DbNotifyAddress address1In = new DbNotifyAddress(address1String);
        DbNotifyAddress address2In = new DbNotifyAddress(address2String);
        
        _database.putNotifyAddresses(ImmutableSet.of(address1In, address2In));
        
        assertNotNull(address1In.getId());
        assertNotNull(address2In.getId());
        assertFalse(address1In.getId().equals(address2In.getId()));
        
        Set<DbNotifyAddress> addresses = _database.getNotifyAddresses();
        assertEquals(2, addresses.size());
        DatabaseTestHelpers.assertSetContainsGivenElements(DB_NOTIFY_ADDRESS_EQ_C, addresses, address1In, address2In);
    }
    
    @Test
    public void testPutGetMultipleNotifyAddressesSequentially() throws SQLException {
        String address1String = "cakehat@cs.brown.edu";
        DbNotifyAddress address1In = new DbNotifyAddress(address1String);
        
        _database.putNotifyAddresses(ImmutableSet.of(address1In));
        assertNotNull(address1In.getId());
        
        Set<DbNotifyAddress> addresses = _database.getNotifyAddresses();
        assertEquals(1, addresses.size());
        DatabaseTestHelpers.assertSetContainsGivenElements(DB_NOTIFY_ADDRESS_EQ_C, addresses, address1In); 
        
        String address2String = "another@email.com";
        DbNotifyAddress address2In = new DbNotifyAddress(address2String);
        
        _database.putNotifyAddresses(ImmutableSet.of(address2In));
        assertNotNull(address2In.getId());
        assertFalse(address1In.getId().equals(address2In.getId()));
        
        addresses = _database.getNotifyAddresses();
        assertEquals(2, addresses.size());
        DatabaseTestHelpers.assertSetContainsGivenElements(DB_NOTIFY_ADDRESS_EQ_C, addresses, address1In, address2In); 
    }
    
    @Test
    public void testPutGetNotifyAddressesFailureAtomicity() throws SQLException {
        String address1String = "cakehat@cs.brown.edu";
        String address2String = "another@email.com";
        int invalidID = -1;
        
        DbNotifyAddress address1In = new DbNotifyAddress(address1String);
        DbNotifyAddress address2In = new DbNotifyAddress(invalidID, address2String);
        
        try {
            _database.putNotifyAddresses(ImmutableSet.of(address1In, address2In));
            fail();
        } catch (SQLException ex) {
            assertEquals(DatabaseTestHelpers.getInvalidIdErrMsg(invalidID), ex.getMessage());
        }
        
        //no notify addresses should be added to the database
        Set<DbNotifyAddress> addresses = _database.getNotifyAddresses();
        assertEquals(0, addresses.size());
    }
    
    @Test
    public void testPutSameNotifyAddressTwice() throws SQLException {
        String addressString = "cakehat@cs.brown.edu";
        
        DbNotifyAddress address1In = new DbNotifyAddress(addressString);
        DbNotifyAddress address2In = new DbNotifyAddress(addressString);
        
        _database.putNotifyAddresses(ImmutableSet.of(address1In, address2In));
        assertNotNull(address1In.getId());
        assertNotNull(address2In.getId());
        assertFalse(address1In.getId().equals(address2In.getId()));
        
        Set<DbNotifyAddress> addresses = _database.getNotifyAddresses();
        assertEquals(2, addresses.size());
        DatabaseTestHelpers.assertSetContainsGivenElements(DB_NOTIFY_ADDRESS_EQ_C, addresses, address1In, address2In);
    }
    
    @Test
    public void testRemoveNonExistentNotifyAddress() throws SQLException {
        int nonExistentId = 1;
        String addressString = "cakehat@cs.brown.edu";
        
        _thrown.expect(SQLException.class);
        _thrown.expectMessage(DatabaseTestHelpers.getInvalidIdErrMsg(nonExistentId));
        
        DbNotifyAddress toRemove = new DbNotifyAddress(nonExistentId, addressString);
        _database.removeNotifyAddresses(ImmutableSet.of(toRemove));
    }
    
    @Test
    public void testPutMultipleGetRemoveOneGetNotifyAddress() throws SQLException {
        DbNotifyAddress address1 = new DbNotifyAddress("cakehat@cs.brown.edu");
        DbNotifyAddress address2 = new DbNotifyAddress("another@email.com");
        
        _database.putNotifyAddresses(ImmutableSet.of(address1, address2));
        Set<DbNotifyAddress> addresses = _database.getNotifyAddresses();
        assertEquals(2, addresses.size());
        DatabaseTestHelpers.assertSetContainsGivenElements(DB_NOTIFY_ADDRESS_EQ_C, addresses, address1, address2);
        
        _database.removeNotifyAddresses(ImmutableSet.of(address1));
        
        addresses = _database.getNotifyAddresses();
        assertEquals(1, addresses.size());
        DatabaseTestHelpers.assertSetContainsGivenElements(DB_NOTIFY_ADDRESS_EQ_C, addresses, address2);
    }
    
}
