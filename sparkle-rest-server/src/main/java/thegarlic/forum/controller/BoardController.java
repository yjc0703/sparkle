package thegarlic.forum.controller;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import thegarlic.forum.Const;
import thegarlic.forum.controller.response.Response;
import thegarlic.forum.domain.Article;
import thegarlic.forum.domain.Board;
import thegarlic.forum.exception.DefaultException;
import thegarlic.forum.repository.ArticleRepository;
import thegarlic.forum.repository.BoardRepository;

@Slf4j
@RestController
@RequestMapping("/boards/{boardName}/articles")
public class BoardController {

    @Autowired
    private ArticleRepository articleRepository;

    @Autowired
    private BoardRepository boardRepository;

    @RequestMapping(value = "/page/{pageNumber}", method = RequestMethod.GET)
    public ResponseEntity<?> getArticles(
            @PathVariable("boardName") String boardName,
            @PathVariable("pageNumber") int pageNumber,
            @RequestParam(value = "sort.order", defaultValue = "writeDate") String sortOrder,
            @RequestParam(value = "sort.direction", defaultValue = "DESC") Direction sortDirection,
            @RequestParam(value = "page.size", defaultValue = Const.ELEMENT_SIZE_PER_PAGE) int pageSize) {

        Board board = getBoard(boardName);

        pageNumber -= 1;
        Sort sort = new Sort(sortDirection, sortOrder);
        PageRequest pageRequest = new PageRequest(pageNumber, pageSize, sort);
        Page<Article> articles = articleRepository.findByBoard(board, pageRequest);
        
        return Response.of(articles);
    }

    @RequestMapping(value = "/{articleId}", method = RequestMethod.GET)
    public ResponseEntity<?> readArticle(
            @PathVariable("boardName") String boardName,
            @PathVariable("articleId") Long articleId) {

        Board board = getBoard(boardName);
        Article article = getArticle(articleId, board);

        return Response.of(article);
    }

    @RequestMapping(value = "/write", method = RequestMethod.POST)
    public ResponseEntity<?> writeArticleByRequestParam(
            @PathVariable("boardName") String boardName,
            Article param) {

        Board board = getBoard(boardName);
        param.setBoard(board);
        Article article = articleRepository.save(param);

        return Response.of(article, HttpStatus.CREATED);
    }

    @RequestMapping(value = "/{articleId}", method = RequestMethod.PUT)
    public ResponseEntity<?> modifyArticle(
            @PathVariable("boardName") String boardName,
            @PathVariable("articleId") Long articleId,
            Article param) {
        
        Board board = getBoard(boardName);
        Article article = getArticle(articleId, board);

        article.setAuthor(param.getAuthor());
        article.setTitle(param.getTitle());
        article.setText(param.getText());
        
        article = articleRepository.save(article);
        
        return Response.of(article, HttpStatus.OK);
    }
    
    private Article getArticle(Long articleId, Board board) {
        Article article = articleRepository.findByIdAndBoard(articleId, board);
        
        if(article == null) {
            throw new DefaultException(String.format("게시글을 찾을 수 없습니다. [ID : %d]", articleId), HttpStatus.NOT_FOUND);
        }
        
        return article;
    }

    private Board getBoard(String boardName) {

        Board board = boardRepository.findByName(boardName);

        if (board == null) {
            log.debug("{} 게시판 조회 실패! exception 발생", boardName);
            throw new DefaultException(String.format("게시판을 찾을 수 없습니다. [boardName : %s]", boardName), HttpStatus.NOT_FOUND);
        }

        return board;
    }
}
