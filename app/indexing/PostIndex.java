package indexing;

import com.github.cleverage.elasticsearch.Index;
import com.github.cleverage.elasticsearch.Indexable;
import com.github.cleverage.elasticsearch.annotations.IndexType;

import java.util.HashMap;
import java.util.Map;

@IndexType(name = "postIndex")
public class PostIndex extends Index {
    public String id;
	public String title;
	public String body;
	public String catId;
	
    // Find method static for request
    public static Finder<PostIndex> find = new Finder<PostIndex>(PostIndex.class);

    @Override
    public Map toIndex() {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("id", id);
        map.put("title", title);
        map.put("body", body);
        map.put("catId", catId);
        return map;
    }

    @Override
    public Indexable fromIndex(Map map) {
        this.id = (String) map.get("id");
        this.title = (String) map.get("title");
        this.body = (String) map.get("body");
        this.catId = (String) map.get("catId");
        return this;
    }
}
