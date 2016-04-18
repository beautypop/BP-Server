package indexing;

import com.github.cleverage.elasticsearch.Index;
import com.github.cleverage.elasticsearch.Indexable;
import com.github.cleverage.elasticsearch.annotations.IndexType;

import java.util.HashMap;
import java.util.Map;

@IndexType(name = "userIndex")
public class UserIndex extends Index {
	public String id;
    public String fname;
	public String lname;
	public String email;

    // Find method static for request
    public static Finder<UserIndex> find = new Finder<UserIndex>(UserIndex.class);

    @Override
    public Map toIndex() {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("id", id);
        map.put("fname", fname);
        map.put("lname", lname);
        map.put("email", email);
        return map;
    }

    @Override
    public Indexable fromIndex(Map map) {
    	this.id = (String) map.get("id");
    	this.fname = (String) map.get("fname");
        this.lname = (String) map.get("lname");
        this.email = (String) map.get("email");
        return this;
    }
}
