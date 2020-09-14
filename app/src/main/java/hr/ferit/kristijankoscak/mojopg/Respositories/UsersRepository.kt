package hr.ferit.kristijankoscak.mojopg.Respositories

import hr.ferit.kristijankoscak.mojopg.Model.User

object UsersRepository {
    var users: MutableList<User>
    init {
        users = mutableListOf()
    }
    fun retreiveUsers(): MutableList<User> {
        return users
    }
    fun saveUsers(users:MutableList<User>){
        this.users.clear()
        this.users = users
    }
    fun remove(id: Int) = users.removeAll{ user -> user.id.toInt() == id }
    fun get(id: Int): User? = users.find { user -> user.id.toInt() == id }
    fun add(user: User) = users.add(user)
    fun getSize():Int {
        return users.size;
    }
}