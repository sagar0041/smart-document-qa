package dev.sagarpatil.docqa.ask;

import dev.sagarpatil.docqa.search.SearchResult;
import java.util.List;

public record AskResponse(
        String answer,
        List<SearchResult> sources
) {}