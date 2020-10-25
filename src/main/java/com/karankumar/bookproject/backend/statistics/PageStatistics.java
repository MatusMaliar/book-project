/*
    The book project lets a user keep track of different books they would like to read, are currently
    reading, have read or did not finish.
    Copyright (C) 2020  Karan Kumar

    This program is free software: you can redistribute it and/or modify it under the terms of the
    GNU General Public License as published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    This program is distributed in the hope that it will be useful, but WITHOUT ANY
    WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
    PURPOSE.  See the GNU General Public License for more details.

    You should have received a copy of the GNU General Public License along with this program.
    If not, see <https://www.gnu.org/licenses/>.
 */

package com.karankumar.bookproject.backend.statistics;

import com.karankumar.bookproject.backend.entity.Book;
import com.karankumar.bookproject.backend.service.PredefinedShelfService;

import java.time.LocalDate;
import java.time.Year;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.karankumar.bookproject.backend.utils.DateUtils.dateIsInCurrentYear;

public class PageStatistics extends Statistics {
    private final List<Book> booksWithPageCount;

    public PageStatistics(PredefinedShelfService predefinedShelfService) {
        super(predefinedShelfService);
        booksWithPageCount = findBooksWithPageCountSpecified();
    }

    /**
     * @return the Book in the 'read' shelf with the highest number of pages
     */
    public Book findBookWithMostPages() {
        Book bookWithMostPages = null;
        if (!booksWithPageCount.isEmpty()) {
            booksWithPageCount.sort(Comparator.comparing(Book::getNumberOfPages));
            bookWithMostPages = booksWithPageCount.get(booksWithPageCount.size() - 1);
        }
        return bookWithMostPages;
    }

    /**
     * @return the Book with the highest number of pages that was read this year
     */
    public Optional<Book> findBookReadThisYearWithTheMostPages() {
        List<Book> booksReadThisYear = booksWithPageCount.stream()
                                         .filter(PageStatistics::readThisYear)
                                         .sorted(Comparator.comparing(Book::getNumberOfPages))
                                         .collect(Collectors.toList());

        return (booksReadThisYear.isEmpty()) ? Optional.empty() :
                Optional.of(booksReadThisYear.get(booksReadThisYear.size() - 1));
    }

    private static boolean readThisYear(Book book) {
        LocalDate dateStarted = book.getDateStartedReading();
        LocalDate dateFinished = book.getDateFinishedReading();
        return dateStarted != null && dateIsInCurrentYear(dateStarted) &&
                dateFinished != null && dateIsInCurrentYear(dateFinished);
    }

    /**
     * @return the Book with the lowest number of pages that was read this year
     */
    public Book findBookWithLeastPages() {
        Book bookWithMostPages = null;
        if (!booksWithPageCount.isEmpty()) {
            booksWithPageCount.sort(Comparator.comparing(Book::getNumberOfPages));
            bookWithMostPages = booksWithPageCount.get(0);
        }
        return bookWithMostPages;
    }

    /**
     * @return the Book in the 'read' shelf with the lowest number of pages this year
     */
    public Book findBookWithLeastPagesThisYear() {
        Book bookWithMostPages = null;
        booksWithPageCount.removeIf(book -> (book.getDateStartedReading() == null ||
                !dateIsInCurrentYear(book.getDateStartedReading()))
        );
        if (!booksWithPageCount.isEmpty()) {
            booksWithPageCount.sort(Comparator.comparing(Book::getNumberOfPages));
            bookWithMostPages = booksWithPageCount.get(0);
        }
        return bookWithMostPages;
    }

    private List<Book> findBooksWithPageCountSpecified() {
        List<Book> booksWithNonEmptyPageCount = new ArrayList<>();
        for (Book book : readShelfBooks) {
            if (book.getNumberOfPages() != null) {
                booksWithNonEmptyPageCount.add(book);
            }
        }
        return booksWithNonEmptyPageCount;
    }

    /**
     * @return the average page length for all books in the 'read' shelf
     * This average only includes books that have a page length specified
     */
    public Double calculateAveragePageLength() {
        int totalNumberOfPages = booksWithPageCount.stream()
                                                   .mapToInt(Book::getNumberOfPages)
                                                   .sum();
        int booksWithPagesSpecified = booksWithPageCount.size();
        if (booksWithPagesSpecified == 0) {
            return null;
        }
        return (booksWithPagesSpecified == 0) ? 0 :
                Math.ceil(totalNumberOfPages / (float) booksWithPagesSpecified);
    }

    /**
     * @return the average page length for all books in the 'read' shelf in this year
     * This average only includes books that have a page length specified
     */
    public Double calculateAveragePageLengthThisYear() {
        booksWithPageCount.removeIf(book ->
                (book.getDateStartedReading() == null ||
                        book.getDateStartedReading().getYear() != Year.now().getValue()));
        int totalNumberOfPages = booksWithPageCount.stream()
                                                   .mapToInt(Book::getNumberOfPages)
                                                   .sum();
        int booksWithPagesSpecified = booksWithPageCount.size();
        if (booksWithPagesSpecified == 0) {
            return null;
        }
        return (booksWithPagesSpecified == 0) ? 0 :
                Math.ceil(totalNumberOfPages / (float) booksWithPagesSpecified);
    }
}
