package com.example.mindlens.screens.splash

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.mindlens.data.onboardingPagesList
import kotlinx.coroutines.launch
import com.example.mindlens.data.OnboardingBodyText
import com.example.mindlens.data.OnboardingPageData
import com.example.mindlens.data.OnboardingTitleText

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(onOnboardingFinished: () -> Unit) {
    val pagerState = rememberPagerState(pageCount = { onboardingPagesList.size })
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 32.dp) // Padding for bottom area
    ) {
        // Top Logo Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp), contentAlignment = Alignment.Center
        ) {
            Text(text = "MindLens", fontWeight = FontWeight.Bold)
        }

        // The Pager (swiping area) takes up most of the space (weight 1f)
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f)
        ) { pageIndex ->
            // This calls the single page layout defined below
            OnboardingSinglePageContent(pageData = onboardingPagesList[pageIndex])
        }

        // Bottom Section: Indicators and Button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Custom Indicators
            PageIndicator(
                pageCount = onboardingPagesList.size,
                currentPage = pagerState.currentPage
            )

            // Next Button
            Button(
                onClick = {
                    coroutineScope.launch {
                        if (pagerState.currentPage < onboardingPagesList.size - 1) {
                            // Scroll to next page
                            pagerState.animateScrollToPage(pagerState.currentPage + 1)
                        } else {
                            // On last page, finish onboarding
                            onOnboardingFinished()
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.height(50.dp)
            ) {
                Text(
                    // Change text on last page if desired, design shows "Next" for all
                    text = if (pagerState.currentPage == onboardingPagesList.size - 1) "Next" else "Next",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// 1. Layout for a single page content within the pager
@Composable
fun OnboardingSinglePageContent(pageData: OnboardingPageData) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = pageData.imageRes),
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp), // Adjust height to fit your illustrations
            contentScale = ContentScale.Fit
        )
        Spacer(modifier = Modifier.height(32.dp))
        OnboardingTitleText(text = pageData.title)
        Spacer(modifier = Modifier.height(16.dp))
        OnboardingBodyText(text = pageData.description)
    }
}

// 2. Custom Row of Dots Indicator
@Composable
fun PageIndicator(pageCount: Int, currentPage: Int) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(pageCount) { iteration ->
            val isSelected = currentPage == iteration
            // The design shows the selected dot as wider (a rounded rect) and others as circles
            Box(
                modifier = Modifier
                    .padding(2.dp)
                    .height(8.dp)
                    .width(if (isSelected) 24.dp else 8.dp) // Wider if selected
                    .clip(CircleShape)
                    .background(if (isSelected) Color.Black else Color.LightGray)
            )
        }
    }
}