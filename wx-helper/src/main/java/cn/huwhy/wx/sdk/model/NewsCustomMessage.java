package cn.huwhy.wx.sdk.model;

import java.util.ArrayList;
import java.util.List;

public class NewsCustomMessage extends CustomMessage {

    private News news = new News();

    public NewsCustomMessage(String toUser) {
        super(toUser, "news");
    }

    public class News {

        private List<Article> articles = new ArrayList<>();

        public List<Article> getArticles() {
            return articles;
        }

        public void setArticles(List<Article> articles) {
            this.articles = articles;
        }

        protected void addArticle(Article article) {
            articles.add(article);
        }
    }

    public News getNews() {
        return news;
    }

    public void setNews(News news) {
        this.news = news;
    }

    public void addArticle(Article article) {
        this.news.addArticle(article);
    }
}
