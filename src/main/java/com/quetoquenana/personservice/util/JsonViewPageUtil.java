package com.quetoquenana.personservice.util;

import com.fasterxml.jackson.annotation.JsonView;
import com.quetoquenana.personservice.model.ApiBaseResponseView;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.NonNull;

import java.util.List;

/**
 * This class wraps the Page object with the JsonView annotation for Execution.
 * It exposes only the fields specified in ExecutionResponseView.
 *
 * @param <T> The type of the content of the Page object.
 */
@JsonView(ApiBaseResponseView.Always.class)
public class JsonViewPageUtil<T> extends PageImpl<T> {

    public JsonViewPageUtil(List<T> content, Pageable pageable, long total) {
        super(content, pageable, total);
    }

    public JsonViewPageUtil(final Page<T> page, final Pageable pageable) {
        super(page.getContent(), pageable, page.getTotalElements());
    }

    public JsonViewPageUtil(List<T> content) {
        super(content);
    }

    @Override
    @JsonView(ApiBaseResponseView.Always.class)
    public int getTotalPages() {
        return super.getTotalPages();
    }

    @Override
    @JsonView(ApiBaseResponseView.Always.class)
    public long getTotalElements() {
        return super.getTotalElements();
    }

    @Override
    @JsonView(ApiBaseResponseView.Always.class)
    public boolean hasNext() {
        return super.hasNext();
    }

    @Override
    @JsonView(ApiBaseResponseView.Always.class)
    public boolean isLast() {
        return super.isLast();
    }

    @Override
    @JsonView(ApiBaseResponseView.Always.class)
    public boolean hasContent() {
        return super.hasContent();
    }

    @Override
    @JsonView(ApiBaseResponseView.Always.class)
    @NonNull
    public List<T> getContent() {
        return super.getContent();
    }
}
