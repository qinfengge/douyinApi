package xyz.qinfengge.douyinapi.controller;

import com.meilisearch.sdk.Client;
import com.meilisearch.sdk.model.SearchResult;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import xyz.qinfengge.douyinapi.result.Result;
import xyz.qinfengge.douyinapi.util.MeilisearchUtil;

/**
 * @Author lza
 * @Date 2023/11/09/21/56
 **/

@RequestMapping("/search")
@RequiredArgsConstructor
@RestController
@CrossOrigin
public class SearchController {

    private final MeilisearchUtil meilisearchUtil;

    @GetMapping()
    @SneakyThrows
    public Result<Object> search(String keyword) {
        Client client = meilisearchUtil.init();
        SearchResult result = client.getIndex("video").search(keyword);
        return Result.ok(result);
    }
}
