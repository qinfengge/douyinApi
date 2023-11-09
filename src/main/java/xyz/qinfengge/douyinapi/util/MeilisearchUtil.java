package xyz.qinfengge.douyinapi.util;

import com.meilisearch.sdk.Client;
import com.meilisearch.sdk.Config;
import com.meilisearch.sdk.Index;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;

/**
 * @Author lza
 * @Date 2023/11/09/19/40
 **/

@Component
public class MeilisearchUtil {

    @SneakyThrows
    public Client init(){
        Config config = new Config("http://localhost:7700", "qinfengge");
        return new Client(config);
    }

    @SneakyThrows
    public Index createIndex(Client client, String indexName){
        return client.index(indexName);
    }

    @SneakyThrows
    public void deleteIndex(Client client, String indexName){
        client.deleteIndex(indexName);
    }

    @SneakyThrows
    public void addDocument(Index index, String doc){
        index.addDocuments(doc);
    }

    @SneakyThrows
    public void deleteDocument(Index index, String docId){
        index.deleteDocument(docId);
    }

    @SneakyThrows
    public void deleteAllDocuments(Index index){
        index.deleteAllDocuments();
    }
}
