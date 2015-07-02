package droid.samepinch.co.data.dao;

import java.util.List;

import droid.samepinch.co.data.dto.Post;

/**
 * Created by cbenjaram on 7/1/15.
 */
public interface IPostDAO {
    Post fetchPostById(String postId);

    List<Post> fetchAllPosts();

    // add post
    boolean addPost(Post post);

    // add posts in bulk
    boolean addPosts(List<Post> posts);

    boolean deleteAllPosts();
}
