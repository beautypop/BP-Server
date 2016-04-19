package indexing;

import com.github.cleverage.elasticsearch.Index;
import com.github.cleverage.elasticsearch.Indexable;
import com.github.cleverage.elasticsearch.annotations.IndexType;

import java.util.HashMap;
import java.util.Map;

@IndexType(name = "userIndex")
public class UserIndex extends Index {
	public String id;
    public String displayName;
	public String firstName;
	public String lastName;

    // Find method static for request
    public static Finder<UserIndex> find = new Finder<UserIndex>(UserIndex.class);

    @Override
    public Map toIndex() {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("id", id);
        map.put("displayName", displayName);
        map.put("firstName", firstName);
        map.put("lastName", lastName);
        return map;
    }

    @Override
    public Indexable fromIndex(Map map) {
    	this.id = (String) map.get("id");
    	this.displayName = (String) map.get("displayName");
        this.firstName = (String) map.get("firstName");
        this.lastName = (String) map.get("lastName");
        return this;
    }
}
