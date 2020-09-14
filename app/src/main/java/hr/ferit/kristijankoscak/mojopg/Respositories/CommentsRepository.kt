package hr.ferit.kristijankoscak.mojopg.Respositories

import hr.ferit.kristijankoscak.mojopg.Model.Comment

object CommentsRepository {
    private var comments: MutableList<Comment>

    init {
        comments = mutableListOf()
    }

    fun retreiveComments(): MutableList<Comment> {
        return comments
    }

    fun saveComments(comments: MutableList<Comment>) {
        this.comments = comments
    }

    fun add(comment: Comment) {
        comments.add(comment)
    }
    fun remove(id: Int) = comments.removeAll{ comment -> comment.id.toInt() == id }

    fun emptyList(){
        comments.clear()
    }
}
