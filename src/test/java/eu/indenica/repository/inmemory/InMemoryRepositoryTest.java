/**
 * 
 */
package eu.indenica.repository.inmemory;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.Collection;

import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Lists;

import eu.indenica.repository.Repository;

/**
 * @author Christian Inzinger
 * 
 */
public class InMemoryRepositoryTest {
    private Repository repository = null;

    @Before
    public void setup() {
        repository = new SampleInMemoryRepository();
    }

    @Test
    public void testAddSimpleObjects() {
        int nObjects = 99;
        {
            for(int i = 0; i < nObjects; i++) {
                String testObj = "foobar" + i;
                repository.store(testObj);
                Collection<String> result =
                        Lists.newArrayList(repository.query(".*"));
                assertThat(result, hasItem(testObj));
            }
            assertThat(repository.query(".*").length, is(nObjects));
        }
        {
            Collection<String> result =
                    Lists.newArrayList(repository.query("foobar5"));

            assertThat(result.size(), is(1));
            assertThat(result, hasItem("foobar5"));
        }
    }
}
