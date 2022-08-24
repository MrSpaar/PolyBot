package api;

@SuppressWarnings("unused")
public class WikipediaApi {
    private Query query;
    private transient String batchcomplete;

    private static class Query {
        private Pages pages;
        private Redirects[] redirects;
        private transient Object normalized;

        private static class Pages {
            private String extract;
            private String title;
            private transient int pageid;
            private transient int ns;
        }

        private static class Redirects {
            private String from;
            private transient String to;
        }
    }

    public String getTitle() {
        return query.pages.title;
    }

    public String getExtract() {
        return query.pages.extract;
    }

    public String getUrl() {
        return "https://fr.wikipedia.org/wiki/" + (query.redirects == null ? getTitle(): query.redirects[0].from);
    }

    public boolean isEmpty() {
        return query == null;
    }
}
