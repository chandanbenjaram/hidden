package droid.samepinch.co.data.dao;

import java.util.List;

import droid.samepinch.co.data.dto.User;

/**
 * Created by cbenjaram on 7/1/15.
 */
public interface IUserDAO {
    User fetchUserById(String userId);

    List<User> fetchAllUsers();

    // add user
    boolean addUser(User user);

    // add users in bulk
    boolean addUsers(List<User> users);

    boolean deleteAllUsers();
}
